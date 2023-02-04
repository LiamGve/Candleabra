package com.ll.candleabra.controller;

import com.ll.candleabra.service.StockEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ControllerV1 {

    private final StockEngineService stockEngineService;

    public ControllerV1(StockEngineService stockEngineService) {
        this.stockEngineService = stockEngineService;
    }

    @GetMapping("/{shortCode}")
    @ResponseStatus(HttpStatus.OK)
    public void getStockTickerInformation(@PathVariable final String shortCode) {
        stockEngineService.processStock(shortCode);
    }
}
