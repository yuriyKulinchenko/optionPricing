package org.example;

import java.util.List;

public class MonteCarloPricer implements DerivativePricer {

    int N = 1000;
    int count = 100;
    double r = 0.05;

    @Override
    public double getPrice(Derivative derivative, StochasticProcess process) {

        double dt = derivative.getMaturity() / count;
        double sum = 0;
        double scalingFactor = Math.exp(-r * derivative.getMaturity()) / N;

        // Carry out N tests
        for(int i = 0; i < N; i++) {
            List<Double> path = process.simulateSteps(count, dt);
            sum += derivative.payoff(path);
            process.reset();
            if(i % 10 == 0 && (i != 0)) {
                System.out.println("Current price: " + scalingFactor * sum * N / i);
            }
        }

        return scalingFactor * sum;
    }

    public void setN(int n) {
        N = n;
    }
}
