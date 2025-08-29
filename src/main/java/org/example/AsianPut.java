package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class AsianPut implements Derivative {
    private final double strikePrice;
    private final double maturity;

    public AsianPut(double strikePrice, double maturity) {
        this.strikePrice = strikePrice;
        this.maturity = maturity;
    }

    @Override
    public double payoff(List<Double> path) {
        double sum = 0;
        for(double x: path) {
            sum += x;
        }
        return Math.max(strikePrice - sum / path.size(), 0);
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
