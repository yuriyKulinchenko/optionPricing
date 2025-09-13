package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public interface Derivative {

    class DerivativePrice {
        double price;
        double delta;

        public DerivativePrice(double price, double delta) {
            this.price = price;
            this.delta = delta;
        }
    }

    DerivativePrice payoff(StochasticProcess process);
    double getMaturity();
}
