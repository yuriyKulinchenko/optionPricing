package org.example;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {

        DerivativePricer pricer = new MonteCarloPricer.Builder()
                .setIterationCount(1_000_000)
                .setSteps(500)
                .setWorkerThreads(4)
                .build();

        Derivative call = new AsianCall(100, 1);

        double mu = UniversalData.riskFreeRate;
        double sigma = 0.25;
        double spot = 100;

//        Supplier<StochasticProcess> supplier = () -> new GeometricBrownianMotion(spot, mu, sigma);
//        double derivativePrice = pricer.getPrice(call, supplier);
//        System.out.println("Final derivative price: " + derivativePrice);

        GeometricBrownianBridge bridge = new GeometricBrownianBridge(spot, mu, sigma);
        double[] randoms = new Sobol(1000, 100).nextNormalVector();
        List<Double> process = bridge.simulateSteps(1000, 0.001, randoms);
        Graph.getInstance().addProcess(process, 0.001);
        Graph.getInstance().draw();
    }
}