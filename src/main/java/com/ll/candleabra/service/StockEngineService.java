package com.ll.candleabra.service;

import com.ll.candleabra.client.StockInformationClient;
import com.ll.candleabra.model.CandleType;
import com.ll.candleabra.model.web.StockIntraDayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

public class StockEngineService {
    private static final Logger log = LoggerFactory.getLogger(StockEngineService.class);

    private final StockInformationClient stockInformationClient;

    public StockEngineService(StockInformationClient stockInformationClient) {
        this.stockInformationClient = stockInformationClient;
    }

    public void processStock(final String shortCode) {
        final StockIntraDayResponse stockDayPerformance = stockInformationClient.getSockInformationFor(shortCode);
        final Map<LocalDateTime, CandleType> candleType = CandleDetectionService.detectCandleTypeAtTime(stockDayPerformance.timeSeries());

        log.info("These candles were found for {}: {}", shortCode, candleType);
    }
}
