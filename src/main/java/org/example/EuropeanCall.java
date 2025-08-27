package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class EuropeanCall implements Derivative {

    private final double strikePrice;

    public EuropeanCall(double strikePrice) {
        this.strikePrice = strikePrice;
    }

    @Override
    public double payoff(List<Double> path) {
        int size = path.size();
        return Math.max(path.get(size - 1) - strikePrice, 0);
    }

    @Override
    public void addChart(XYChart chart) {
        // TODO: Implement visualization for call option
    }
}
