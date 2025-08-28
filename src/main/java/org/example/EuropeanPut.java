package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class EuropeanPut implements  Derivative {

    private final double strikePrice;
    private final double maturity;

    public static class Builder implements BuilderInterface<EuropeanPut> {

        public double strikePrice;
        public double maturity;

        public Builder() {
            this.strikePrice = 0;
            this.maturity = 0;
        }

        Builder setStrikePrice(double strikePrice) {
            this.strikePrice = strikePrice;
            return this;
        }

        Builder setMaturity(double maturity) {
            this.maturity = maturity;
            return this;
        }

        public EuropeanPut build() {
            return new EuropeanPut(this);
        }
    }

    public EuropeanPut(Builder builder) {
        this.strikePrice = builder.strikePrice;
        this.maturity = builder.maturity;
    }

    @Override
    public double payoff(List<Double> path) {
        return Math.max(strikePrice - path.getLast(), 0);
    }

    public double getMaturity() {
        return maturity;
    }

    @Override
    public void addChart(XYChart chart) {
        // TODO: Implement visualization for put option
    }
}
