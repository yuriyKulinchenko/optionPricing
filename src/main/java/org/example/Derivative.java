package org.example;

public abstract class Derivative {

    public static class DerivativePrice {
        public double price;
        public double delta;
        public double rho;
        public double theta;
        public double vega;

        public DerivativePrice(
                double price,
                double delta,
                double rho,
                double theta,
                double vega
        ) {
            this.price = price;
            this.delta = delta;
            this.rho = rho;
            this.theta = theta;
            this.vega = vega;
        }
    }

    abstract double rawPayoff(StochasticProcess process);
    abstract double payoffDerivative(StochasticProcess process, int i);
    abstract double getMaturity();

    DerivativePrice payoff(StochasticProcess process) {
        return process.payoff(this); // Arguably messy?
    }
}
