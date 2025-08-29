package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class EuropeanCall implements Derivative {

    private final double strikePrice;
    private final double maturity;

    public EuropeanCall(double strikePrice, double maturity) {
        this.strikePrice = strikePrice;
        this.maturity = maturity;
    }

    @Override
    public double payoff(List<Double> path) {
        return Math.max(path.getLast() - strikePrice, 0);
    }

    public double getMaturity() {
        return maturity;
    }

    @Override
    public void addChart(XYChart chart) {
        // TODO: Implement visualization for call option
    }
}
