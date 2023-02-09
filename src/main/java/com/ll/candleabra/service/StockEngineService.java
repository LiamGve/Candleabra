package com.ll.candleabra.service;

import com.ll.candleabra.client.StockInformationClient;
import com.ll.candleabra.model.CandleType;
import com.ll.candleabra.model.OwnedStock;
import com.ll.candleabra.model.StockIncrementInformation;
import com.ll.candleabra.model.web.StockIntraDayResponse;
import com.ll.candleabra.model.web.StockTickerIncrement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.ll.candleabra.model.CandleType.BEARISH_DOJI;
import static com.ll.candleabra.model.CandleType.BEARISH_ENGULFING;
import static com.ll.candleabra.model.CandleType.BULLISH_DOJI;
import static com.ll.candleabra.model.CandleType.BULLISH_ENGULFING;
import static com.ll.candleabra.model.CandleType.HAMMER;
import static com.ll.candleabra.model.CandleType.SHOOTING_STAR;
import static com.ll.candleabra.model.web.StockTickerIncrement.Action.BUY;
import static com.ll.candleabra.model.web.StockTickerIncrement.Action.NOTHING;
import static com.ll.candleabra.model.web.StockTickerIncrement.Action.SELL;
import static org.springframework.util.CollectionUtils.isEmpty;

public class StockEngineService {
    private static final Logger log = LoggerFactory.getLogger(StockEngineService.class);

    private final StockInformationClient stockInformationClient;
    private final TopicResponseService topicResponseService;

    public StockEngineService(StockInformationClient stockInformationClient,
                              TopicResponseService topicResponseService) {
        this.stockInformationClient = stockInformationClient;
        this.topicResponseService = topicResponseService;
    }

    @Async
    public void processStock(final String shortCode,
                             final float investmentAmount) {
        final StockIntraDayResponse stockDayPerformance = stockInformationClient.getSockInformationFor(shortCode); // TODO replace with a realtime websocket to data

        final StockWindow sw = new StockWindow(5);
        final AtomicReference<Float> currentMoney = new AtomicReference<>(investmentAmount);
        final List<OwnedStock> stockBought = new ArrayList<>();


        final List<Map.Entry<LocalDateTime, StockIncrementInformation>> stocksInTimeOrder = new ArrayList<>(stockDayPerformance.timeSeries().entrySet());

        stocksInTimeOrder.sort(Map.Entry.comparingByKey());

        stocksInTimeOrder.forEach(entry -> {
            sw.add(entry.getValue());

            final CandleType candleType;
            if (sw.size() == 5) {
                LinkedList<StockIncrementInformation> window = sw.getWindow();
                candleType = CandleDetectionService.detectCandleType(window.remove(), window.remove(), window.remove(), window.remove(), window.remove());
                log.info("These candles were found for {}: {} (time={})", shortCode, candleType, entry.getKey());
            } else {
                candleType = CandleType.NOTHING;
            }


            float price = entry.getValue().close();

            StockTickerIncrement.Action action = NOTHING;

            if (HAMMER.equals(candleType) || BEARISH_DOJI.equals(candleType) || BEARISH_ENGULFING.equals(candleType)) {
                log.info("Logging buy at {}", price);
                float amount = (float) Math.floor(currentMoney.get() / price);
                if (currentMoney.get() >= (price * amount) && amount != 0f) {
                    currentMoney.set(currentMoney.get() - (price * amount));
                    stockBought.add(new OwnedStock(shortCode, amount, price));
                    action = BUY;
                } else {
                    log.warn("Not enough liquid cash ({}) to purchase this amount of stock {} (at ${})", currentMoney.get(), amount, price);
                }
            } else if (SHOOTING_STAR.equals(candleType) || BULLISH_DOJI.equals(candleType) || BULLISH_ENGULFING.equals(candleType)) {
                log.info("Logigng sale at {}", price);
                if (!isEmpty(stockBought)) {
                    for (OwnedStock stockOwned : stockBought) {
                        float moneyMade = (price - stockOwned.price()) * stockOwned.amount();
                        float moneyOriginallyInvested = stockOwned.price() * stockOwned.amount();

                        currentMoney.set(currentMoney.get() + moneyMade + moneyOriginallyInvested);
                    }
                    stockBought.clear();
                    action = SELL;
                } else {
                    log.warn("Nothing to sell...");
                }
            }

            final StockTickerIncrement si = new StockTickerIncrement(
                    entry.getValue().open(),
                    entry.getValue().close(),
                    entry.getValue().high(),
                    entry.getValue().low(),
                    entry.getValue().volume(),
                    entry.getKey(),
                    candleType,
                    action,
                    stockBought,
                    currentMoney.get());

            try {
                Thread.sleep(500);
                topicResponseService.sendStockTick(si);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

//        simulateMarketActivity(stockDayPerformance.timeSeries(), candleType, investmentAmount, shortCode);
    }
//
//    public MarketOutcome simulateMarketActivity(final Map<LocalDateTime, StockIncrementInformation> timeSeries,
//                                                final Map<LocalDateTime, CandleType> candles,
//                                                final float totalAmountWillingToInvest,
//                                                final String stockShortCode) {
//        final AtomicReference<Float> currentMoney = new AtomicReference<>(totalAmountWillingToInvest);
//        final List<OwnedStock> stockBought = new ArrayList<>();
//
//        final List<Map.Entry<LocalDateTime, StockIncrementInformation>> stocksInTimeOrder = new ArrayList<>(timeSeries.entrySet());
//        stocksInTimeOrder.sort(Map.Entry.comparingByKey());
//
//        stocksInTimeOrder.forEach(stockTick -> {
//            if (candles.containsKey(stockTick.getKey())) {
//                float price = value.close();
//
//                if (HAMMER.equals(c) || BEARISH_DOJI.equals(c) || BEARISH_ENGULFING.equals(c)) {
//                    log.info("Logging buy at {}", price);
//                    float amount = (float) Math.floor(currentMoney.get() / price);
//                    if (currentMoney.get() >= (price * amount) && amount != 0f) {
//                        currentMoney.set(currentMoney.get() - (price * amount));
//                        stockBought.add(new OwnedStock(stockShortCode, amount, price));
//                    } else {
//                        log.warn("Not enough liquid cash ({}) to purchase this amount of stock {} (at ${})", currentMoney.get(), amount, price);
//                    }
//                } else if (SHOOTING_STAR.equals(c) || BULLISH_DOJI.equals(c) || BULLISH_ENGULFING.equals(c)) {
//                    log.info("Logigng sale at {}", price);
//                    if (!isEmpty(stockBought)) {
//                        for (OwnedStock stockOwned : stockBought) {
//                            float moneyMade = (price - stockOwned.price()) * stockOwned.amount();
//                            float moneyOriginallyInvested = stockOwned.price() * stockOwned.amount();
//
//                            currentMoney.set(currentMoney.get() + moneyMade + moneyOriginallyInvested);
//                        }
//                        stockBought.clear();
//                    } else {
//                        log.warn("Nothing to sell...");
//                    }
//                }
//
//            }
//        });
//
//        Float valueInStock = stockBought.stream().map(ownedStock -> ownedStock.price() * ownedStock.amount()).reduce(Float::sum).orElse(0f);
//        return new MarketOutcome(currentMoney.get(), stockBought, currentMoney.get() + valueInStock);
//    }
}
