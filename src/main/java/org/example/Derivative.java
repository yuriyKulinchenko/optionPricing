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
        int L = process.path.length;
        double[] adjointList = new double[L];
        adjointList[L - 1] = payoffDerivative(process, L - 1);
        for(int i = L - 2; i >= 0; i--) {
            adjointList[i] = adjointList[i + 1] * process.stepDerivative(i + 1)
                    + payoffDerivative(process, i);
        }
        
        double delta = adjointList[0];

        return new DerivativePrice(rawPayoff(process), delta);
    }
}
