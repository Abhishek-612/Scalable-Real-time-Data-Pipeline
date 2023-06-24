package com.pipeline.datapipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.pipeline.datapipeline.beans.DataModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
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

    @Value("${data-models-directory}")
    private String DATA_MODELS_DIRECTORY;

    private final ObjectMapper objectMapper;

    public DataModelLoader() {
        this.objectMapper = new ObjectMapper();
    }

    public List<DataModel> loadAndParseDataModels() {
        File dataModelDirectory = new File(DATA_MODELS_DIRECTORY);
        List<DataModel> listOfDataModels = new ArrayList<>();

        if (dataModelDirectory.exists() && dataModelDirectory.isDirectory()) {
            File[] dataModelFiles = dataModelDirectory.listFiles();
            if (dataModelFiles != null) {
                for (File dataModelFile : dataModelFiles) {
                    try {
                        System.out.println(dataModelFile);
                        JsonNode dataModelNode = objectMapper.readTree(dataModelFile);
                        System.out.println(dataModelNode);

                        // Process the data model definition as needed
                        listOfDataModels.add(parseDataModel(dataModelNode));
                    } catch (IOException e) {
                        LOGGER.error("Failed to parse data model definition file: " + dataModelFile.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        return listOfDataModels;
    }

    private DataModel parseDataModel(JsonNode dataModelNode) {
        System.out.println(DATA_MODELS_DIRECTORY);
        // Extract base configuration
        String name = dataModelNode.get("name").asText();
        System.out.println(DATA_MODELS_DIRECTORY);
        String api = dataModelNode.get("api").asText();

        // Extract schema information
        JsonNode dataModelConfig = dataModelNode.get("schema");

        // Create the DataModel object with the extracted information
        return new DataModel(name, api, dataModelConfig);
    }

}