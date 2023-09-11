package org.acme.lambda;

import java.util.Map;

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
import org.apache.camel.support.builder.Namespaces;

@Startup
@ApplicationScoped
public class SplitCamelRoute extends RouteBuilder {
    @Inject
    CamelContext camelContext;

    public static final String PRODUCT_COUNT = "productCount";
    public static final String PRODUCT_BATCH_SIZE = "productBatchSize";
    public static final String ADDRESS_COUNT = "addressCount";
    public static final String ADDRESS_BATCH_SIZE = "addressBatchSize";
    public static final String FROM = "from";
    public static final String PRODUCT = "product";
    public static final String ADDRESS = "address";

    @Override
    public void configure() {
        from("direct:input") // .split(stax(Person.class)).streaming()
                .multicast()
                .streaming()
                .parallelProcessing()
                .stopOnException().to("direct:product", "direct:address").end();

        from("direct:product") // .split(stax(Person.class)).streaming()
                .setProperty(FROM, constant(PRODUCT))
                .setProperty(PRODUCT_COUNT, constant(0))
                .setProperty(PRODUCT_BATCH_SIZE, constant(3))
                .split()
                .xtokenize("ns1:data/products/product", 'i', new Namespaces()).streaming()
                .aggregate(constant(true), new SplitterAggregationStrategy())
                .completionPredicate(new SplitterCompletionPredicate(PRODUCT_COUNT, PRODUCT_BATCH_SIZE))
                .forceCompletionOnStop()
                .to("file:/tmp?fileName=out-product-${exchangeProperty.productCount}.xml&fileExist=Append&charset=UTF-8")
                .end();

        from("direct:address") // .split(stax(Person.class)).streaming()
                .setProperty(FROM, constant(ADDRESS))
                .setProperty(ADDRESS_COUNT, constant(0))
                .setProperty(ADDRESS_BATCH_SIZE, constant(1))
                .split()
                .xtokenize("ns1:data/addresses/address", 'i', new Namespaces()).streaming()
                .aggregate(constant(true), new SplitterAggregationStrategy())
                .completionPredicate(new SplitterCompletionPredicate(ADDRESS_COUNT, ADDRESS_BATCH_SIZE))
                .forceCompletionOnStop()
                .to("file:/tmp?fileName=out-address-${exchangeProperty.addressCount}.xml&fileExist=Append&charset=UTF-8")
                .end();
    }

    static int incrementCount(final Map<String, Object> propertyMap) {
        // Increment the 'recordCount' property
        final String from = (String) propertyMap.get(FROM);
        final String fromCount = from.equals(PRODUCT) ? PRODUCT_COUNT : ADDRESS_COUNT;
        final int count = (int) propertyMap.get(fromCount) + 1;
        propertyMap.put(fromCount, count);
        return count;
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class SplitterCompletionPredicate implements Predicate {
        final String countProperty;
        final String batchProperty;

        @Override
        public boolean matches(final Exchange exchange) {
            final int count = exchange.getProperty(countProperty, 0, Integer.class);
            final int batchSize = exchange.getProperty(batchProperty, Integer.class);
            log.info("countProperty: {}, count: {}, batchProperty: {}, batchSize: {}", countProperty, count, batchProperty, batchSize);
            final boolean isSplitComplete = exchange.getProperty(Exchange.SPLIT_COMPLETE, false, Boolean.class);
            return count % batchSize == 0 || isSplitComplete;
        }
    }

    @Slf4j
    public static class SplitterAggregationStrategy implements AggregationStrategy {
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
            final int incrementCount = incrementCount(oldExchange.getProperties());
            log.info("incremented: {}", incrementCount);
            return oldExchange;
        }
    }
}


