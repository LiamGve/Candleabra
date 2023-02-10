package com.ll.candleabra.test;

import com.ll.candleabra.model.web.request.StockIncrementInformation;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.LocalDateTime.now;

public final class TestUtil {

    public static StockIncrementInformation stockIncrement(float open,
                                                           float high,
                                                           float low,
                                                           float close,
                                                           float volume) {
        return new StockIncrementInformation(open, high, low, close, volume);
    }

    public static Map<LocalDateTime, StockIncrementInformation> stockWindow(StockIncrementInformation one,
                                                                            StockIncrementInformation two,
                                                                            StockIncrementInformation three,
                                                                            StockIncrementInformation four,
                                                                            StockIncrementInformation five) {
        return Map.of(
                now().minusMinutes(4), one,
                now().minusMinutes(3), two,
                now().minusMinutes(2), three,
                now().minusMinutes(1), four,
                now(), five);
    }
}
