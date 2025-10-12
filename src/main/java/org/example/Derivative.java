package org.example;

public abstract class Derivative {

    public static class DerivativePrice {
        public double price;
        public PathwiseGreeks pathwiseGreeks;
        public LogScore logScore;

        public DerivativePrice(double price, PathwiseGreeks pathwiseGreeks, LogScore logScore) {
            this.price = price;
            this.pathwiseGreeks = pathwiseGreeks;
            this.logScore = logScore;
        }
    }

    public static class PathwiseGreeks {
        public double delta;
        public double rho;
        public double theta;
        public double vega;

        public PathwiseGreeks(double delta, double rho, double theta, double vega) {
            this.delta = delta;
            this.rho = rho;
            this.theta = theta;
            this.vega = vega;
        }
    }

    public static class LogScore {
        public double delta;
        public double rho;
        public double theta;
        public double vega;

        public LogScore(double delta, double rho, double theta, double vega) {
            this.delta = delta;
            this.rho = rho;
            this.theta = theta;
            this.vega = vega;
        }
    }



    abstract double rawPayoff(StochasticProcess process);
    abstract double payoffDerivative(StochasticProcess process, int i);
    abstract double getMaturity();

    // The following methods use arguably too much indirection: They are here for backwards compatibility

    DerivativePrice payoff(StochasticProcess process) {
        // Should be overridden for derivatives for which pathwise greeks cannot be computed

        PathwiseGreeks pathwiseGreeks = process.getPathwiseGreeks(this);
        LogScore logScore = process.getLogScore();

        return new DerivativePrice(rawPayoff(process), pathwiseGreeks, logScore);
    }
}
