package com.foo;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.foo.model.UserSales;
import com.foo.service.UserSalesService;

@Path("/sales")
public class UserSalesResource {
    @Inject
    UserSalesService userSalesService;

    @Path("/{userId}/{productId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UserSales get(@PathParam("userId") String userId, @PathParam("productId") String productId) {
        return userSalesService.process(userId, productId);
    }
}