package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {

        Derivative call = new EuropeanPut(1.2, 1);

        DerivativePricer pricer = new MonteCarloPricer.Builder()
                .setIterationCount(10_000_000)
                .setSteps(1)
                .setWorkerThreads(4)
                .build();

        double mu = UniversalData.riskFreeRate;
        double sigma = UniversalData.volatility;
        double spot = 1;

        Supplier<StochasticProcess> supplier = () -> new GeometricBrownianMotion(spot, mu, sigma);
        double derivativePrice = pricer.getPrice(call, supplier);
        System.out.println("Final derivative price: " + derivativePrice);

    }
}