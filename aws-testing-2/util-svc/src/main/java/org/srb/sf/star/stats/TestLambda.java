package org.srb.sf.star.stats;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

@Named("test")
public class TestLambda implements RequestHandler<InputObject, OutputObject> {

    @Inject
    ProcessingService service;

    @Override
    public OutputObject handleRequest(final InputObject input, final Context context) {
        return service.process(input).setRequestId(context.getAwsRequestId());
    }
}
