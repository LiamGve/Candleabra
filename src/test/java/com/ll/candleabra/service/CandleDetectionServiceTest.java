package com.ll.candleabra.service;

import com.ll.candleabra.model.CandleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static com.ll.candleabra.test.TestUtil.stockIncrement;
import static com.ll.candleabra.test.TestUtil.stockWindow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandleDetectionServiceTest {

    @Test
    void detectHammerCandlestick() {
        Map<LocalDateTime, CandleType> response = CandleDetectionService.detectCandleTypeAtTime(
                stockWindow(
                        stockIncrement(10, 0, 10, 9, 0),
                        stockIncrement(9, 0, 10, 8, 0),
                        stockIncrement(8, 0, 10, 7, 0),
                        stockIncrement(10, 0, 1, 6, 1),
                        stockIncrement(0, 0, 7, 0, 0)
                )
        );

        assertTrue(response.values().stream().anyMatch(value -> value.equals(CandleType.HAMMER)));
    }

    @Test
    void noHammerCandlestickDetected() {
        Map<LocalDateTime, CandleType> response = CandleDetectionService.detectCandleTypeAtTime(
                stockWindow(
                        stockIncrement(0, 0, 10, 9, 0),
                        stockIncrement(9, 0, 10, 8, 0),
                        stockIncrement(8, 0, 10, 0, 0),
                        stockIncrement(10, 0, 1, 6, 1),
                        stockIncrement(0, 0, 7, 0, 0)
                )
        );

        assertFalse(response.values().stream().anyMatch(value -> value.equals(CandleType.HAMMER)));
    }

}