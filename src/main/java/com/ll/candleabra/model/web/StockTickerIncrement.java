package com.ll.candleabra.model.web;

import com.ll.candleabra.model.CandleType;
import com.ll.candleabra.model.OwnedStock;

import java.time.LocalDateTime;
import java.util.List;

public record StockTickerIncrement(float open,
                                   float close,
                                   float high,
                                   float low,
                                   float volume,
                                   LocalDateTime timestamp,
                                   CandleType candlePattern,
                                   Action action,
                                   List<OwnedStock> investments,
                                   float liquidCash) {
    public enum Action {
        BUY,
        SELL,
        SHORT,
        NOTHING
    }
}
