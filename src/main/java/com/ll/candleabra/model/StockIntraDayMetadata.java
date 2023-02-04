package com.ll.candleabra.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StockIntraDayMetadata(@JsonProperty("1. Information") String information,
                                    @JsonProperty("2. Symbol") String symbol,
                                    @JsonProperty("3. Last Refreshed") String lastRefreshed,
                                    @JsonProperty("4. Interval") String interval,
                                    @JsonProperty("5. Output Size") String outputSize,
                                    @JsonProperty("6. Time Zone") String timeZone) {

}
