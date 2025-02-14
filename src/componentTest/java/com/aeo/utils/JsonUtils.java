package com.aeo.utils;

import com.aeo.constants.Constants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode getPropertyByHierarchy(ObjectNode baseNode, String property) {
        JsonNode currentProperty = baseNode;
        for (String i : property.split("\\.")) {
            currentProperty = currentProperty.get(i);
        }
        return currentProperty;
    }

    public static <T> T readFromJson(String file, Class<T> toValueType) throws IOException, URISyntaxException {
        return (T) objectMapper.readValue(new URI("file:"
                + Constants.RESOURCES_PATH
                + "/json/"
                + file).toURL(), toValueType);
    }

    public static boolean compareJsonStructures(ObjectNode node1, ObjectNode node2) {
        // checkstyle rules don't allow 4 return
        boolean result = node1.size() == node2.size();

        Iterator<String> fieldNames = node1.fieldNames();

        while (result
                && fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            if (!node2.has(fieldName)) {
                result = false;
                break;
            }
            JsonNode value1 = node1.get(fieldName);
            JsonNode value2 = node2.get(fieldName);

            if (value1.getNodeType() != value2.getNodeType()
                    || (value1.isObject()
                            && !compareJsonStructures((ObjectNode) value1, (ObjectNode) value2))) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * This method changes json property value.
     *
     * @param json     Json in ObjectNode format
     * @param property Property name to change. Can be given with a hierarchy (separated by dots): "amount.currency"
     * @param newValue new property value
     */
    public static void changeJsonProperty(ObjectNode json, String property, String newValue) {
        String propertyName = property.contains(".") ? property.substring(property.lastIndexOf('.') + 1) : property;

        String hierarchy = property.contains(".") ? property.substring(0, property.lastIndexOf('.')) : "";

        JsonNode parentNode = hierarchy.length() > 0 ? JsonUtils.getPropertyByHierarchy(json, hierarchy) : json;

        ((ObjectNode) parentNode).put(propertyName, newValue);
    }

}
