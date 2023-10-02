package com.foo.service;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import com.foo.model.UserSales;

@ApplicationScoped
public class UserSalesService {
    public UserSales process(@NonNull final String userId, @NonNull final String productId) {
        return UserSales.builder()
                .userId(userId)
                .productId(productId)
                .build();
    }
}
