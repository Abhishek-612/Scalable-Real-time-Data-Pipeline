package com.pipeline.datapipeline.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.pipeline.datapipeline.beans.DataModel;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class DataModelLoader {

    private final Logger LOGGER = LogManager.getLogger();
    private final HashMap<String, DataModel> listOfDataModels = new HashMap<>();

    @Value("${data-models-directory}")
    private String DATA_MODELS_DIRECTORY;

    private final ObjectMapper objectMapper;

    public DataModelLoader() {
        this.objectMapper = new ObjectMapper();
    }

    public DataModel getDataModelByName(String apiName) {
        return this.listOfDataModels.getOrDefault(apiName, null);
    }

    public void loadAndParseDataModels() {
        File dataModelDirectory = new File(DATA_MODELS_DIRECTORY);

        if (dataModelDirectory.exists() && dataModelDirectory.isDirectory()) {
            File[] dataModelFiles = dataModelDirectory.listFiles();
            if (dataModelFiles != null) {
                for (File dataModelFile : dataModelFiles) {
                    try {
                        System.out.println(dataModelFile);
                        JsonNode dataModelNode = objectMapper.readTree(dataModelFile);
                        System.out.println(dataModelNode);

                        // Process the data model definition as needed
                        listOfDataModels.put(dataModelNode.get("name").asText(), parseDataModel(dataModelNode));
                    } catch (IOException e) {
                        LOGGER.error("Failed to parse data model definition file: " + dataModelFile.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private DataModel parseDataModel(JsonNode dataModelNode) {
        // Extract base configuration
        String name = dataModelNode.get("name").asText();
        String api = dataModelNode.get("api").asText();
        Integer fetchInteval = dataModelNode.get("fetch_interval").asInt(Constants.DEFAULT_API_STREAM_FETCH_INTERVAL);
        Integer restartDelay = dataModelNode.get("restart_delay").asInt(Constants.DEFAULT_API_STREAM_RESTART_DELAY);

        // Extract headers information
        JsonNode headersNode = dataModelNode.get("headers");
        HttpHeaders httpHeaders = new HttpHeaders();
        for (JsonNode headerNode : headersNode) {
            String headerName = headerNode.get("name").asText();
            String headerValue = headerNode.get("value").asText();
            httpHeaders.add(headerName, headerValue);
        }

        // Extract schema information
        JsonNode dataModelConfig = dataModelNode.get("schema");

        // Create the DataModel object with the extracted information
        return new DataModel(name, api, httpHeaders, fetchInteval, restartDelay, dataModelConfig);
    }

}