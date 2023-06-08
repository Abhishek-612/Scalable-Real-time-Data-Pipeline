package com.pipeline.datapipeline.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pipeline.datapipeline.beans.DataModel;
import com.pipeline.datapipeline.services.DataStreamerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ApiStreamController {

    private final DataStreamerService dataStreamerService;

    @Autowired
    public ApiStreamController(DataStreamerService dataStreamerService) {
        this.dataStreamerService = dataStreamerService;
    }

    public void streamApiData(DataModel dataModel) {
        // Configure the WebClient to make API requests
        WebClient webClient = WebClient.create();

        // Make the API request and stream the response
        Mono<JsonNode> apiData = webClient.get()
                .uri(dataModel.getApi())
                .retrieve()
                .bodyToMono(JsonNode.class);

        // Process and stream the API data using DataStreamerService
        apiData.subscribe(data -> dataStreamerService.processData(dataModel, data));
    }
}