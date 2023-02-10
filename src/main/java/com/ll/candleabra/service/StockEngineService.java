package com.ll.candleabra.service;

import com.ll.candleabra.client.StockInformationClient;
import com.ll.candleabra.model.Action;
import com.ll.candleabra.model.CandleType;
import com.ll.candleabra.model.OwnedStock;
import com.ll.candleabra.model.StockCandleItem;
import com.ll.candleabra.model.web.request.StockIncrementInformation;
import com.ll.candleabra.model.web.request.StockIntraDayResponse;
import com.ll.candleabra.model.web.response.StockTickerIncrement;
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
            sw.add(new StockCandleItem(
                    entry.getValue().high(),
                    entry.getValue().low(),
                    entry.getValue().open(),
                    entry.getValue().close(),
                    entry.getValue().volume(),
                    entry.getKey()
            ));

            final CandleType candleType;
            if (sw.size() == 5) {
                LinkedList<StockCandleItem> window = sw.getWindow();
                candleType = CandleDetectionService.detectCandleType(window.remove(), window.remove(), window.remove(), window.remove(), window.remove());
                log.info("These candles were found for {}: {} (time={})", shortCode, candleType, entry.getKey());
            } else {
                candleType = CandleType.NOTHING;
            }


            float price = entry.getValue().close();

            final Action action;
            if (HAMMER.equals(candleType) || BEARISH_DOJI.equals(candleType) || BEARISH_ENGULFING.equals(candleType)) {
                log.info("Logging buy at {}", price);
                float amount = (float) Math.floor(currentMoney.get() / price);
                if (currentMoney.get() >= (price * amount) && amount != 0f) {
                    currentMoney.set(currentMoney.get() - (price * amount));
                    stockBought.add(new OwnedStock(shortCode, amount, price));
                    action = new Action(Action.Type.BUY, amount, price, shortCode, entry.getKey());
                } else {
                    log.warn("Not enough liquid cash ({}) to purchase this amount of stock {} (at ${})", currentMoney.get(), amount, price);
                    action = new Action(Action.Type.NOTHING, 0, price, shortCode, entry.getKey());
                }
            } else if (SHOOTING_STAR.equals(candleType) || BULLISH_DOJI.equals(candleType) || BULLISH_ENGULFING.equals(candleType)) {
                log.info("Logging sale at {}", price);
                if (!isEmpty(stockBought)) {
                    for (OwnedStock stockOwned : stockBought) {
                        float moneyMade = (price - stockOwned.price()) * stockOwned.amount();
                        float moneyOriginallyInvested = stockOwned.price() * stockOwned.amount();

                        currentMoney.set(currentMoney.get() + moneyMade + moneyOriginallyInvested);
                    }
                    action = new Action(
                            Action.Type.SELL,
                            stockBought.stream().map(OwnedStock::amount).reduce(Float::sum).orElse(0f),
                            price,
                            shortCode,
                            entry.getKey());
                    stockBought.clear();
                } else {
                    log.warn("Nothing to sell...");
                    action = new Action(Action.Type.NOTHING, 0, price, shortCode, entry.getKey());
                }
            } else {
                action = new Action(Action.Type.NOTHING, 0, price, shortCode, entry.getKey());
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

//            try {
//                Thread.sleep(500);
                topicResponseService.sendStockTick(si);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        });
    }
}
