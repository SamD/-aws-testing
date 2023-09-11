package org.acme.config;

import java.text.DateFormat;
import java.time.ZoneOffset;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonUtil {
    public final TimeZone utcTimeZone = TimeZone.getTimeZone(ZoneOffset.UTC);
    public final DateFormat defaultDateFormat = new StdDateFormat();

    private ObjectMapper defaultObjectMapper;

    public JsonMapper.Builder getDefaultObjectMapperBuilder() {
        return JsonMapper.builder()
                .defaultDateFormat(defaultDateFormat)
                .defaultTimeZone(utcTimeZone)
                .enable(getBuilderDefaultSerializationFeaturesEnabled())
                .enable(getBuilderDefaultDeserializationFeaturesEnabled())
                .enable(getBuilderDefaultMapperFeaturesEnabled())
                .disable(getBuilderDefaultSerializationFeaturesDisabled())
                .disable(getBuilderDefaultDeserializationFeaturesDisabled())
                .disable(getBuilderDefaultMapperFeaturesDisabled())
                .addModules(getBuilderDefaultModules())
                .serializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Synchronized
    public ObjectMapper getDefaultObjectMapper() {
        if (defaultObjectMapper == null) {
            defaultObjectMapper = getDefaultObjectMapperBuilder().build();
        }
        // return a copy so as not to modify original
        return defaultObjectMapper.copy();
    }

    private SerializationFeature[] getBuilderDefaultSerializationFeaturesEnabled() {
        return new SerializationFeature[]{
        };
    }

    private DeserializationFeature[] getBuilderDefaultDeserializationFeaturesEnabled() {
        return new DeserializationFeature[]{
        };
    }

    private MapperFeature[] getBuilderDefaultMapperFeaturesEnabled() {
        return new MapperFeature[]{
        };
    }

    private SerializationFeature[] getBuilderDefaultSerializationFeaturesDisabled() {
        return new SerializationFeature[]{
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.WRITE_DATES_WITH_ZONE_ID,
                SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE,
                SerializationFeature.FAIL_ON_EMPTY_BEANS,
        };
    }

    private DeserializationFeature[] getBuilderDefaultDeserializationFeaturesDisabled() {
        return new DeserializationFeature[]{
                DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        };
    }

    private MapperFeature[] getBuilderDefaultMapperFeaturesDisabled() {
        return new MapperFeature[]{
                // We don't want jackson determining what to use as a creater based on
                // @ConstructorProperties essentially this means if enabled that the
                // @ConstructorProperties will be used in the Serialization/Deserialization
                // process overriding what you might provide using Jackson Annotations
                // This is specific to lombok where it adds @ConstructorProperties
                // Additionally see: https://github.com/FasterXML/jackson-databind/issues/1371
                MapperFeature.INFER_CREATOR_FROM_CONSTRUCTOR_PROPERTIES
        };
    }

    private Module[] getBuilderDefaultModules() {
        return new Module[]{
                new Jdk8Module(),
                new JavaTimeModule()
                // new Jackson2HalModule(),
        };
    }

}
