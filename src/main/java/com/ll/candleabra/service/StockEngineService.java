package com.ll.candleabra.service;

import com.ll.candleabra.client.StockInformationClient;
import com.ll.candleabra.model.CandleType;
import com.ll.candleabra.model.MarketOutcome;
import com.ll.candleabra.model.OwnedStock;
import com.ll.candleabra.model.StockIncrementInformation;
import com.ll.candleabra.model.web.StockIntraDayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.ll.candleabra.model.CandleType.BEARISH_DOJI;
import static com.ll.candleabra.model.CandleType.BEARISH_ENGULFING;
import static com.ll.candleabra.model.CandleType.BULLISH_DOJI;
import static com.ll.candleabra.model.CandleType.BULLISH_ENGULFING;
import static com.ll.candleabra.model.CandleType.HAMMER;
import static com.ll.candleabra.model.CandleType.SHOOTING_STAR;
import static org.springframework.util.CollectionUtils.isEmpty;

public class StockEngineService {
    private static final Logger log = LoggerFactory.getLogger(StockEngineService.class);

    private final StockInformationClient stockInformationClient;

    public StockEngineService(StockInformationClient stockInformationClient) {
        this.stockInformationClient = stockInformationClient;
    }

    public MarketOutcome processStock(final String shortCode,
                                      final float investmentAmount) {
        final StockIntraDayResponse stockDayPerformance = stockInformationClient.getSockInformationFor(shortCode);
        final Map<LocalDateTime, CandleType> candleType = CandleDetectionService.detectCandleTypeAtTime(stockDayPerformance.timeSeries());

        log.info("These candles were found for {}: {}", shortCode, candleType);

        return simulateMarketActivity(stockDayPerformance.timeSeries(), candleType, investmentAmount, shortCode);
    }

    public MarketOutcome simulateMarketActivity(final Map<LocalDateTime, StockIncrementInformation> timeSeries,
                                                final Map<LocalDateTime, CandleType> candles,
                                                final float totalAmountWillingToInvest,
                                                final String stockShortCode) {
        final AtomicReference<Float> currentMoney = new AtomicReference<>(totalAmountWillingToInvest);
        final List<OwnedStock> stockBought = new ArrayList<>();

        final List<Map.Entry<LocalDateTime, StockIncrementInformation>> stocksInTimeOrder = new ArrayList<>(timeSeries.entrySet());
        stocksInTimeOrder.sort(Map.Entry.comparingByKey());

        stocksInTimeOrder.forEach(stockTick -> {
            if (candles.containsKey(stockTick.getKey())) {
                final CandleType c = candles.get(stockTick.getKey());
                float price = timeSeries.get(stockTick.getKey()).close();

                if (HAMMER.equals(c) || BEARISH_DOJI.equals(c) || BEARISH_ENGULFING.equals(c)) {
                    log.info("Logging buy at {}", price);
                    float amount = (float) Math.floor(currentMoney.get() / price);
                    if (currentMoney.get() >= (price * amount)) {
                        currentMoney.set(currentMoney.get() - (price * amount));
                        stockBought.add(new OwnedStock(stockShortCode, amount, price));
                    } else {
                        log.warn("Not enough liquid cash ({}) to purchase this amount of stock {} (at ${})", currentMoney.get(), amount, price);
                    }
                } else if (SHOOTING_STAR.equals(c) || BULLISH_DOJI.equals(c) || BULLISH_ENGULFING.equals(c)) {
                    log.info("Logigng sale at {}", price);
                    if (!isEmpty(stockBought)) {
                        for (OwnedStock stockOwned : stockBought) {
                            float moneyMade = (price - stockOwned.price()) * stockOwned.amount();
                            float moneyOriginallyInvested = stockOwned.price() * stockOwned.amount();

                            currentMoney.set(currentMoney.get() + moneyMade + moneyOriginallyInvested);
                        }
                        stockBought.clear();
                    } else {
                        log.warn("Nothing to sell...");
                    }
                }

            }
        });

        Float valueInStock = stockBought.stream().map(ownedStock -> ownedStock.price() * ownedStock.amount()).reduce(Float::sum).orElse(0f);
        return new MarketOutcome(currentMoney.get(), stockBought, currentMoney.get() + valueInStock);
    }
}
