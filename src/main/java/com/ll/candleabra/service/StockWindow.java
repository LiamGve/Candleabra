package com.ll.candleabra.service;

import com.ll.candleabra.model.StockCandleItem;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.LinkedList;
import java.util.Queue;

public class StockWindow {
    private final Queue<StockCandleItem> window;

    public StockWindow(final int windowSize) {
        this.window = new CircularFifoQueue<>(windowSize);
    }

    public void add(StockCandleItem stockTickerIncrement) {
        window.add(stockTickerIncrement);
    }

    public LinkedList<StockCandleItem> getWindow() {
        return new LinkedList<>(window);
    }

    public int size() {
        return window.size();
    }
}
