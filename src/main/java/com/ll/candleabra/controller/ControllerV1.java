package com.ll.candleabra.controller;

import com.ll.candleabra.model.web.request.WebSocketMessage;
import com.ll.candleabra.service.StockEngineService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ControllerV1 {

    private final StockEngineService stockEngineService;

    public ControllerV1(StockEngineService stockEngineService) {
        this.stockEngineService = stockEngineService;
    }

    @MessageMapping("/start")
    public String startTailingStock(WebSocketMessage webSocketMessage) {
        stockEngineService.processStock(
                webSocketMessage.shortCode(),
                webSocketMessage.investmentAmount());
        return "process-started";
    }
}
