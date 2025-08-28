package org.example;

import java.util.List;

public class MonteCarloPricer implements DerivativePricer {

    int N;
    int steps;
    double r;

    public static class Builder implements  BuilderInterface<MonteCarloPricer> {

        int N;
        int steps;

        public Builder() {
            N = 1000;
            steps = 100;
        }

        Builder setIterationCount(int N) {
            this.N = N;
            return this;
        }

        Builder setSteps(int steps) {
            this.steps = steps;
            return this;
        }

        @Override
        public MonteCarloPricer build() {
            return new MonteCarloPricer(this);
        }
    }

    public MonteCarloPricer(Builder builder) {
        this.N = builder.N;
        this.steps = builder.steps;
    }

    @Override
    public double getPrice(Derivative derivative, StochasticProcess process) {

        this.r = process.drift;

        double dt = derivative.getMaturity() / steps;
        double sum = 0;
        double scalingFactor = Math.exp(-r * derivative.getMaturity()) / N;

        // Carry out N tests
        for(int i = 0; i < N; i++) {
            List<Double> path = process.simulateSteps(steps, dt);
            sum += derivative.payoff(path);
            if(i % 10 == 0 && (i != 0)) {
                System.out.println("Current price: " + scalingFactor * sum * N / i);
            }
            process.reset();
        }

        return scalingFactor * sum;
    }
}
