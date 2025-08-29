package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {

        Derivative call = new EuropeanCall.Builder()
                .setMaturity(1)
                .setStrikePrice(1.2)
                .build();

        DerivativePricer pricer = new MonteCarloPricer.Builder()
                .setIterationCount(10000)
                .setWorkerThreads(4)
                .build();

        double mu = 0.05;
        double sigma = 0.15;
        double spot = 1;

        Supplier<StochasticProcess> supplier = () -> new GeometricBrownianMotion(spot, mu, sigma);
        double derivativePrice = pricer.getPrice(call, supplier);
        System.out.println("Final derivative price: " + derivativePrice);

    }
}