package com.ll.candleabra.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.candleabra.client.StockInformationClient;
import com.ll.candleabra.controller.ControllerV1;
import com.ll.candleabra.service.StockEngineService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BootstrapConfiguration {

    @Bean
    public StockInformationClient stockClient(final ObjectMapper objectMapper) {
        return new StockInformationClient(new RestTemplate(), objectMapper);
    }

    @Bean
    public StockEngineService stockEngineService(final StockInformationClient stockInformationClient) {
        return new StockEngineService(stockInformationClient);
    }

    @Bean
    public ControllerV1 controller(final StockEngineService stockEngineService) {
        return new ControllerV1(stockEngineService);
    }
}
