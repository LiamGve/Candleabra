package com.ll.candleabra.service;

import com.ll.candleabra.model.web.response.StockTickerIncrement;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class TopicResponseService {
    private final SimpMessagingTemplate client;

    public TopicResponseService(SimpMessagingTemplate client) {
        this.client = client;
    }

    public void sendStockTick(final StockTickerIncrement stockIncrement) {
        client.convertAndSend("/topic/stock-tick", stockIncrement);
    }
}
