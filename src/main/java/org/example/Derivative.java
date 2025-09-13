package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public abstract class Derivative {

    public static class DerivativePrice {
        public double price;
        public double delta;

        public DerivativePrice(double price, double delta) {
            this.price = price;
            this.delta = delta;
        }
    }

    abstract double rawPayoff(StochasticProcess process);
    abstract double payoffDerivative(StochasticProcess process, int i);
    abstract double getMaturity();

    DerivativePrice payoff(StochasticProcess process) {
        // Assume rawPayoff and payoffDerivative are NOT discounted
        double price = rawPayoff(process);
        double delta = 0;

        return new DerivativePrice(price, delta);
    }
}
