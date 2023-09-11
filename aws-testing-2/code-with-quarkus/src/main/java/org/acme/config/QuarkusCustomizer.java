package org.acme.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.All;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

public class QuarkusCustomizer {
    @Singleton
    @Produces
    ObjectMapper objectMapper(@All final List<ObjectMapperCustomizer> customizers) {
        final ObjectMapper mapper = CommonUtil.getDefaultObjectMapper();

        // Apply all default customizers provided by Quarkus
        customizers.forEach(objectMapperCustomizer -> objectMapperCustomizer.customize(mapper));
        return mapper;
    }
}
