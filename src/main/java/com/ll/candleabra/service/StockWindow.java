package com.ll.candleabra.service;

import com.ll.candleabra.model.StockIncrementInformation;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.LinkedList;
import java.util.Queue;

public class StockWindow {
    private final Queue<StockIncrementInformation> window;

    public StockWindow(final int windowSize) {
        this.window = new CircularFifoQueue<>(windowSize);
    }

    public void add(StockIncrementInformation stockTickerIncrement) {
        window.add(stockTickerIncrement);
    }

    public LinkedList<StockIncrementInformation> getWindow() {
        return new LinkedList<>(window);
    }

    public int size() {
        return window.size();
    }
}
