package org.example;

public abstract class Derivative {

    public static class DerivativePrice {
        public double price;
        public GreekData.PathwiseGreeks pathwiseGreeks;
        public GreekData.LogScore logScore;

        public DerivativePrice(
                double price,
                GreekData.PathwiseGreeks pathwiseGreeks,
                GreekData.LogScore logScore
        ) {
            this.price = price;
            this.pathwiseGreeks = pathwiseGreeks;
            this.logScore = logScore;
        }
    }


    abstract double rawPayoff(StochasticProcess process);
    abstract double payoffDerivative(StochasticProcess process, int i);
    abstract double getMaturity();

    // The following methods use arguably too much indirection: They are here for backwards compatibility

    DerivativePrice payoff(StochasticProcess process) {
        // Should be overridden for derivatives for which pathwise greeks cannot be computed

        GreekData.PathwiseGreeks pathwiseGreeks = process.getPathwiseGreeks(this);
        GreekData.LogScore logScore = process.getLogScore();

        return new DerivativePrice(rawPayoff(process), pathwiseGreeks, logScore);
    }
}
