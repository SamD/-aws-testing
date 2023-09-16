package org.acme.lambda;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregateController;
import org.apache.camel.processor.aggregate.DefaultAggregateController;
import org.apache.camel.support.builder.Namespaces;
import org.apache.commons.lang3.StringUtils;

@Startup
@ApplicationScoped
@Slf4j
public class SplitCamelRoute extends RouteBuilder {
    @Inject
    CamelContext camelContext;

    private final AggregateController aggregateController = new DefaultAggregateController();

    public static final String PRODUCT_COUNT = "productCount";
    public static final String AGGREGATED_COMPLETION_SIZE = "aggregatedCompletionSize";
    public static final String PRODUCT_BATCH_SIZE = "productBatchSize";
    public static final String ADDRESS_COUNT = "addressCount";
    public static final int ADDRESS_GROUP_SIZE = 2;
    public static final String FROM = "from";
    public static final String PRODUCT = "product";
    public static final String ADDRESS = "address";

    @Override
    public void configure() {
        from("direct:input") // .split(stax(Person.class)).streaming()
                .multicast()
                .streaming()
                .parallelProcessing()
                .stopOnException()
//                .to("direct:product")
                .to("direct:address")
                .end();

        from("direct:product") // .split(stax(Person.class)).streaming()
                .setProperty(FROM, constant(PRODUCT))
                .setProperty(PRODUCT_BATCH_SIZE, constant(3))
                .split()
                .xtokenize("ns1:data/products/product", 'i', new Namespaces()).streaming()
                .aggregate(new SplitterCompletionPredicateAndAggregationStrategy(PRODUCT_COUNT, PRODUCT_BATCH_SIZE)).constant(PRODUCT)
                .completionSize(header(AGGREGATED_COMPLETION_SIZE))
                .aggregateController(aggregateController)
                .eagerCheckCompletion()
                .forceCompletionOnStop()
                .to("file:/tmp?fileName=out-product-${exchangeProperty.CamelSplitIndex}.xml&fileExist=Append&charset=UTF-8")
                .end();

        from("direct:address") // .split(stax(Person.class)).streaming()
//                .filter(xpath("//*").not(constant("products")))
                .setProperty(FROM, constant(ADDRESS))
                .split()
                .xtokenize("ns1:data/addresses/address", 'i', new Namespaces(), ADDRESS_GROUP_SIZE)
                .streaming()
                .process(exchange -> {
                    final String body = (String) exchange.getIn().getBody();
                    String newBody = StringUtils.removeStart(body, "<group>");
                    newBody = StringUtils.removeEnd(newBody, "</group>");
                    exchange.getIn().setBody(newBody);
                })
                .log("BODY: ${body}")
                .to("file:/tmp?fileName=out-address-${exchangeProperty.CamelSplitIndex}.xml&fileExist=Append&charset=UTF-8")
                .end();
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class SplitterCompletionPredicateAndAggregationStrategy implements Predicate, AggregationStrategy {
        final String countProperty;
        final String batchProperty;

        @Override
        public boolean matches(final Exchange exchange) {
            return matchesOrCanComplete(exchange);
        }

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                // Initialize the aggregated exchange
                oldExchange = newExchange.copy();
                oldExchange.getIn().setBody(newExchange.getIn().getBody(String.class));
            } else {
                // Append the body of the new exchange to the existing aggregated body
                final String aggregatedBody = oldExchange.getIn().getBody(String.class);
                final String newBody = newExchange.getIn().getBody(String.class);
                oldExchange.getIn().setBody(
                        String.join("\n", aggregatedBody, newBody)
                );
            }

            return oldExchange;
        }

        boolean matchesOrCanComplete(final Exchange exchange) {
            if (exchange == null) return false;
            log.info("props: {}", exchange.getAllProperties());
            log.info("headers: {}", exchange.getIn().getHeaders());

            final String from = (String) exchange.getProperty(FROM);

            final int batchSize = exchange.getProperty(batchProperty, Integer.class);
            final boolean isSplitComplete = exchange.getProperty(Exchange.SPLIT_COMPLETE, false, Boolean.class);
            final int splitIndex = exchange.getProperty(Exchange.SPLIT_INDEX, 0, Integer.class);
            final int splitSize = exchange.getProperty(Exchange.SPLIT_SIZE, 0, Integer.class);
            final boolean isBatchComplete = exchange.getProperty(Exchange.BATCH_COMPLETE, false, Boolean.class);
            final int aggregatedSize = exchange.getProperty(Exchange.AGGREGATED_SIZE, 0, Integer.class);

            boolean result = (splitIndex % batchSize == 0) || (isSplitComplete || isBatchComplete);

            // this is necessary for the last case where the split can be complete but the batch size is never met
            if (isSplitComplete) {
                log.info("[matchesOrCanComplete] split complete setting size: {}", aggregatedSize);
                exchange.getIn().setHeader(AGGREGATED_COMPLETION_SIZE, aggregatedSize);
            }

            log.info(
                    "from: {}, batchProperty: {}, countProperty: {}, batchSize: {}, batchComplete: {}, splitComplete: {}, splitIndex: {}, splitSize: {}, aggregatedSize: {}, result: {}",
                    from, batchProperty, countProperty, batchSize, isBatchComplete, isSplitComplete, splitIndex, splitSize, aggregatedSize, result
            );

            return result;
        }

        public static int incrementCount(final Exchange exchange) {
            // Increment the 'recordCount' property
            final String from = exchange.getProperty(FROM, String.class);
            final String fromCount = from.equals(PRODUCT) ? PRODUCT_COUNT : ADDRESS_COUNT;
            final int count = exchange.getIn().getHeader(fromCount, 0, Integer.class) + 1;
            exchange.getIn().setHeader(fromCount, count);
            return count;
        }

    }
}


