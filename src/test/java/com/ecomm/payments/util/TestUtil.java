package com.ecomm.payments.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

public class TestUtil {

    @SuppressWarnings("unchecked")
    public static <T> T getResponseFromJsonPath(String path, Class<T> expectedReturnClass) {
        try (FileInputStream fis = new FileInputStream(ResourceUtils.getFile(String.format("classpath:%s", path)))) {
            String jsonValue = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
            return (T) jsonToObject(jsonValue, expectedReturnClass);
        } catch (IOException e) {
            throw new RuntimeException("Convert Cart Json response error");
        }
    }

    public static Object jsonToObject(String json, Class<?> type) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final SimpleModule zonedDateTimeSerialization = new SimpleModule();
            zonedDateTimeSerialization.addDeserializer(ZonedDateTime.class, new ZonedDateTimeCustomDeserializer());
            mapper.registerModule(zonedDateTimeSerialization);
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(json, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
