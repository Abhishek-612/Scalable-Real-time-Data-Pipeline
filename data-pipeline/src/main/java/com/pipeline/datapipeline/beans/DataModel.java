package com.pipeline.datapipeline.beans;


import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.MethodSpec;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataModel {
    private String name;
    private String api;
    private HttpHeaders httpHeaders;
    private int fetchInterval;
    private int restartDelay;
    private JsonNode schema;
    private Object dataModel;

    private List<MethodSpec> getterSetterMethods;


    public DataModel(String name, String api, HttpHeaders httpHeaders, int fetchInterval, int restartDelay, JsonNode schema, List<MethodSpec> getterSetterMethods) {
        this.name = name;
        this.api = api;
        this.httpHeaders = httpHeaders;
        this.fetchInterval = fetchInterval;
        this.restartDelay = restartDelay;
        this.schema = schema;
        this.getterSetterMethods = getterSetterMethods;
        this.dataModel = extractSchemaInfo(this.schema);
    }


    // Getters and setters

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public int getFetchInterval() {
        return fetchInterval;
    }

    public void setFetchInterval(int fetchInterval) {
        this.fetchInterval = fetchInterval;
    }

    public int getRestartDelay() {
        return restartDelay;
    }

    public void setRestartDelay(int restartDelay) {
        this.restartDelay = restartDelay;
    }


    private Object extractSchemaInfo(JsonNode propertyNode) {
        if (propertyNode.isArray()) {
            // If the propertyNode is an array, it represents an array of data model configurations
            // You can process each element of the array and return a List or custom object
            List<Object> arrayDataModel = new ArrayList<>();
            for (JsonNode elementNode : propertyNode) {
                // Recursively extract the data model configuration for each element
                Object elementDataModel = extractSchemaInfo(elementNode);
                arrayDataModel.add(elementDataModel);
            }
            return arrayDataModel;
        } else if (propertyNode.isObject()) {
            // If the propertyNode is an object, it represents a nested data model configuration
            // You can recursively process it and return a Map or custom object
            Map<String, Object> nestedDataModel = new HashMap<>();
            propertyNode.fields().forEachRemaining(entry -> {
                String nestedPropertyName = entry.getKey();
                JsonNode nestedPropertyNode = entry.getValue();
                // Recursively extract the nested property information
                Object nestedPropertyInfo = extractSchemaInfo(nestedPropertyNode);
                nestedDataModel.put(nestedPropertyName, nestedPropertyInfo);
            });
            return nestedDataModel;
        } else {
            // For simple properties, extract the necessary information based on the property type
            // For example, you can check the property type and convert it accordingly
            if (propertyNode.isTextual()) {
                return propertyNode.asText();
            } else if (propertyNode.isBoolean()) {
                return propertyNode.asBoolean();
            } else if (propertyNode.isInt()) {
                return propertyNode.asInt();
            } else if (propertyNode.isDouble()) {
                return propertyNode.asDouble();
            }
            // Handle other property types as needed

            // If none of the types match, return null or throw an exception based on your requirement
            return null;
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public Object getDataModel() {
        return dataModel;
    }

    public void setDataModel(Object dataModel) {
        this.dataModel = dataModel;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public void setSchema(JsonNode schema) {
        this.schema = schema;
    }

    public List<MethodSpec> getGetterSetterMethods() {
        return getterSetterMethods;
    }

    public void setGetterSetterMethods(List<MethodSpec> getterSetterMethods) {
        this.getterSetterMethods = getterSetterMethods;
    }
}