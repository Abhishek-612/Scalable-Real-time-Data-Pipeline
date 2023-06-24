package com.pipeline.datapipeline.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pipeline.datapipeline.beans.DataModel;
import com.pipeline.datapipeline.services.DataStreamerService;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
public class ApiStreamController {

    private static final Logger LOGGER = LogManager.getLogger();
    private final DataStreamerService dataStreamerService;
    private final WebClient webClient;
    private final int TIMEOUT_SECONDS = 10;
    private final int BUFFER_LIMIT = 10 * 1024 * 1024;  // Set buffer limit to 10 MB
    private Duration restartDelay = Duration.ofSeconds(30);

    @Autowired
    public ApiStreamController(DataStreamerService dataStreamerService, WebClient.Builder webClientBuilder) {
        this.dataStreamerService = dataStreamerService;
        this.webClient = webClientBuilder.build();
    }

    public void startStreaming(DataModel dataModel) {
        this.restartDelay = Duration.ofSeconds(dataModel.getRestartDelay());
        Mono<JsonNode> apiData = fetchData(dataModel);

        Flux.interval(Duration.ofSeconds(dataModel.getFetchInterval()))
                .onBackpressureDrop() // Drop ticks when downstream can't keep up
                .flatMap(tick -> apiData.onErrorResume(error -> handleError(error, dataModel)))
                .subscribeOn(Schedulers.parallel()) // Execute the stream on a parallel scheduler
                .subscribe(data -> dataStreamerService.processData(dataModel, data));
    }

    private Mono<JsonNode> fetchData(DataModel dataModel) {
        {
            HttpClient httpClient = HttpClient.create().wiretap(true).responseTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT_SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(TIMEOUT_SECONDS)))
                    .tcpConfiguration(tcpClient -> tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                            .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(TIMEOUT_SECONDS))
                                    .addHandlerLast(new WriteTimeoutHandler(TIMEOUT_SECONDS)))
                            .option(ChannelOption.SO_KEEPALIVE, true))
                    .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT_SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(TIMEOUT_SECONDS)));

            WebClient.Builder webClientBuilder = WebClient.builder()
                    .baseUrl(dataModel.getApi())
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .exchangeStrategies(ExchangeStrategies.builder()
                            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(BUFFER_LIMIT))
                            .build());

            return webClientBuilder.build()
                    .get()
                    .uri(dataModel.getApi())
                    .header("X-RapidAPI-Host", "twelve-data1.p.rapidapi.com")
                    .header("X-RapidAPI-Key", "9a15fa944dmsh3b965dab06fa0cfp1e0460jsnaac0d5930b98")
                    .retrieve()
                    .bodyToMono(JsonNode.class);
        }
    }

    private Mono<JsonNode> handleError(Throwable error, DataModel dataModel) {
        LOGGER.info(restartDelay.getSeconds());
        LOGGER.error("Error occurred while fetching {} API data: {}", dataModel.getName(), error.getMessage());
        LOGGER.error("Restarting the {} thread after {} seconds...", dataModel.getName(), restartDelay.getSeconds());

        return Mono.delay(restartDelay)
                .flatMap(tick -> fetchData(dataModel))
                .onErrorResume(e -> handleError(e, dataModel));
    }
}
