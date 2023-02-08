package com.ll.candleabra.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.candleabra.client.StockInformationClient;
import com.ll.candleabra.controller.ControllerV1;
import com.ll.candleabra.service.StockEngineService;
import com.ll.candleabra.service.TopicResponseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@EnableAsync
@Configuration
public class BootstrapConfiguration {

    @Bean
    public TopicResponseService topicResponseService(final SimpMessagingTemplate simpMessagingTemplate) {
        return new TopicResponseService(simpMessagingTemplate);
    }

    @Bean
    public StockInformationClient stockClient(final ObjectMapper objectMapper) {
        return new StockInformationClient(new RestTemplate(), objectMapper);
    }

    @Bean
    public StockEngineService stockEngineService(final StockInformationClient stockInformationClient,
                                                 final TopicResponseService topicResponseService) {
        return new StockEngineService(stockInformationClient, topicResponseService);
    }

    @Bean
    public ControllerV1 controller(final StockEngineService stockEngineService) {
        return new ControllerV1(stockEngineService);
    }
}
