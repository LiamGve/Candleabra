package com.ll.candleabra.model;

import java.time.LocalDateTime;

public record StockCandleItem(float high,
                              float low,
                              float open,
                              float close,
                              float volume,
                              LocalDateTime timestamp) {
}
