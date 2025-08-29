package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class AsianCall implements Derivative {

    private final double strikePrice;
    private final double maturity;

    public AsianCall(double strikePrice, double maturity) {
        this.strikePrice = strikePrice;
        this.maturity = maturity;
    }

    @Override
    public double payoff(List<Double> path) {
        double sum = 0;
        for(double x: path) {
            sum += x;
        }
        return Math.max(sum / path.size() - strikePrice, 0);
    }

    @Override
    public double getMaturity() {
        return maturity;
    }

    @Override
    public void addChart(XYChart chart) {
        // TODO: Implement visualization for asian call option
    }
}
