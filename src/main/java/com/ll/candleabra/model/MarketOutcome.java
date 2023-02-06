package com.ll.candleabra.model;

import java.util.List;

public record MarketOutcome(float liquidCash,
                            List<OwnedStock> moneyInStock,
                            float portfolioValue) {
}
