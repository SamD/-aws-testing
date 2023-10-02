package org.srb.sf.star.stats;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.quarkus.amazon.lambda.http.model.AwsProxyRequest;
import io.quarkus.amazon.lambda.http.model.AwsProxyResponse;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("test")
public class ProxyLambda implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {

    @Inject
    ProcessingService service;

    @Override
    public AwsProxyResponse handleRequest(final AwsProxyRequest awsProxyRequest, final Context context) {
        return service.process(input).setRequestId(context.getAwsRequestId());
    }
}
