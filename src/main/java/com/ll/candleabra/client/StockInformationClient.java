package com.ll.candleabra.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.ll.candleabra.model.web.StockIntraDayResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

public class StockInformationClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public StockInformationClient(RestTemplate restTemplate,
                                  ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public StockIntraDayResponse getSockInformationFor1(final String shortCode) {
        return restTemplate.getForObject(format("""
                https://www.alphavantage.co/query
                      ?function=TIME_SERIES_INTRADAY
                      &symbol=%s
                      &interval=5min
                      &apikey=%s
                """, shortCode, "apiKey"), StockIntraDayResponse.class);
    }

    public StockIntraDayResponse getSockInformationFor(final String shortCode) {
        try {
            final URL path = Resources.getResource(format("mock-%s-stock.json", shortCode));
            return objectMapper.readValue(Resources.toString(path, StandardCharsets.UTF_8), StockIntraDayResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
