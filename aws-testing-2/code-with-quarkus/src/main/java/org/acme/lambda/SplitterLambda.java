package org.acme.lambda;

import java.io.IOException;
import java.net.URL;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.common.io.Resources;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.camel.ProducerTemplate;

@Named("test")
public class SplitterLambda implements RequestHandler<Object, String> {
    @Inject
    ProducerTemplate template;

    @Override
    // public String handleRequest(final Person input, final Context context) {
    public String handleRequest(final Object input, final Context context) {
        final LambdaLogger logger = context.getLogger();
        logger.log("Calling Camel Route :)");

        final URL url = Resources.getResource("test.xml");
        try {
            return template.requestBody("direct:input", url.openStream(), String.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
