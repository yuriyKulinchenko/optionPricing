package org.example;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        double mu = 0.05;
        double sigma = 0.15;
        double spot = 1;
        double dt = 0.01; // 0.01 years

        StochasticProcess stock = new GeometricBrownianMotion(spot, mu, sigma);
        Derivative call = new EuropeanCall(1.2, 10);
        DerivativePricer pricer = new MonteCarloPricer();
        double derivativePrice = pricer.getPrice(call, stock);
        System.out.println("Final derivative price: " + derivativePrice);
    }
}