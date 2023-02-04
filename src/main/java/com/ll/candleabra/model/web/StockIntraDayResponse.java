package com.ll.candleabra.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ll.candleabra.model.StockIncrementInformation;
import com.ll.candleabra.model.StockIntraDayMetadata;

import java.time.LocalDateTime;
import java.util.Map;

public record StockIntraDayResponse(@JsonProperty("Meta Data") StockIntraDayMetadata metadata,
                                    @JsonProperty("Time Series (5min)") Map<LocalDateTime, StockIncrementInformation> timeSeries) {
}
