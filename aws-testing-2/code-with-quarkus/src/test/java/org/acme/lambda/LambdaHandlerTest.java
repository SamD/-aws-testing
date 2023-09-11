package org.acme.lambda;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.quarkus.amazon.lambda.test.LambdaClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@Slf4j
public class LambdaHandlerTest {
    @Inject
    CamelContext camelContext;


    @Test
    public void fileTest() throws Exception {
        // final URL url = Resources.getResource("test.xml");

        final NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();

        /*
         * try (final InputStream in = url.openStream()) { final String contents = Resources.toString(url,
         * StandardCharsets.UTF_8); }
         */
        final CompletableFuture<String> foo = LambdaClient.invokeAsync(String.class, "foo");
        final String invoke = foo.get();

        boolean done = notify.matches(10, TimeUnit.SECONDS);
        assertTrue(done);

        // log.info("*** INVOKE: {}", invoke);

        /*
         * Person in = new Person(); in.setName("Stu");
         * given().contentType("application/json").accept("application/json").body(in).when().post().then().statusCode(
         * 200) .body(containsString("Hello Stu"));
         */
    }

}
