package com.ll.candleabra.model.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

public record StockIntraDayResponse(@JsonProperty("Meta Data") Object metadata,
                                    @JsonProperty("Weekly Time Series") Map<LocalDateTime, StockIncrementInformation> timeSeries) {
}
