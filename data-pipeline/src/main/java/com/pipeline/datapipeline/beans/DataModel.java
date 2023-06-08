package com.pipeline.datapipeline.beans;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataModel {
    private String name;
    private String api;
    private JsonNode schema;

    private Object dataModel;

    public DataModel(String name, String api, JsonNode schema) {
        this.name = name;
        this.api = api;
        this.schema = schema;
        this.dataModel = extractSchemaInfo(this.schema);
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

    // Getters and setters

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

    public Object getDataModelConfig() {
        return dataModel;
    }

    public void setDataModelConfig(Object dataModel) {
        this.dataModel = dataModel;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public void setSchema(JsonNode schema) {
        this.schema = schema;
    }
}