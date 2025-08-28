package org.example;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        double mu = 0.05;
        double sigma = 0.15;
        double spot = 1;

        StochasticProcess stock = new GeometricBrownianMotion(spot, mu, sigma);

        Derivative call = new EuropeanCall.Builder()
                .setMaturity(1)
                .setStrikePrice(1.2)
                .build();

        DerivativePricer pricer = new MonteCarloPricer.Builder()
                .setIterationCount(10000)
                .build();


//        double derivativePrice = pricer.getPrice(call, stock);
//        System.out.println("Final derivative price: " + derivativePrice);

        List<Double> distribution = new ArrayList<>();
        for(int i = 0; i < 100000; i++) {
            distribution.add(RandomGenerator.getInstance().nextGaussian());
        }

        Graph.drawDistribution(distribution, 100);
    }
}