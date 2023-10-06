package com.pipeline.datapipeline.webcomponents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/api/v1")
public class MenuOptions {

    private static final Logger LOGGER = LogManager.getLogger();
    private final RestTemplate restTemplate;
    private final String pipelineBaseUrl;

    @Autowired
    public MenuOptions(RestTemplate restTemplate, @Value("${app.baseurl}") String pipelineBaseUrl) {
        this.restTemplate = restTemplate;
        this.pipelineBaseUrl = pipelineBaseUrl;
    }

    @GetMapping("/")
    public String getMenuOptions() {
        return "main_menu";
    }

    @PostMapping("/stream")
    public String startStream(@RequestParam("apiName") String apiName) {
        restTemplate.postForObject(pipelineBaseUrl + "/stream?apiName=" + apiName, null, String.class);
        return "redirect:/api/v1";
    }

    @PostMapping("/fetch")
    public String fetchData(@RequestParam("apiName") String apiName) {
        restTemplate.postForObject(pipelineBaseUrl + "/fetch?apiName=" + apiName, null, String.class);
        return "redirect:/api/v1";
    }
}