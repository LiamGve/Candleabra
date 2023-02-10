package com.ll.candleabra.model;

import java.time.LocalDateTime;

public record Action(Type type,
                     float amount,
                     float price,
                     String stock,
                     LocalDateTime timestamp) {

    public enum Type {
        NOTHING,
        BUY,
        SELL
    }
}
