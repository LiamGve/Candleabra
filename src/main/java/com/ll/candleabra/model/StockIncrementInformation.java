package com.ll.candleabra.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StockIncrementInformation(@JsonProperty("1. open") float open,
                                        @JsonProperty("2. high") float high,
                                        @JsonProperty("3. low") float low,
                                        @JsonProperty("4. close") float close,
                                        @JsonProperty("5. volume") float volume) {
}
