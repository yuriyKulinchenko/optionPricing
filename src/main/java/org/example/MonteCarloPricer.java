package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;

public class MonteCarloPricer implements DerivativePricer {

    int N; // N is per thread
    int steps;
    int workerThreads;

    public static class Builder implements  BuilderInterface<MonteCarloPricer> {

        int N;
        int steps;
        int workerThreads;

        public Builder() {
            N = 1000;
            steps = 100;
            workerThreads = Runtime.getRuntime().availableProcessors() / 2;
        }

        Builder setIterationCount(int N) {
            this.N = N;
            return this;
        }

        Builder setSteps(int steps) {
            this.steps = steps;
            return this;
        }

        Builder setWorkerThreads(int workerThreads) {
            this.workerThreads = workerThreads;
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
        this.workerThreads = builder.workerThreads;
    }

    @Override
    public double getPrice(Derivative derivative, Supplier<StochasticProcess> processSupplier) {

        DoubleAdder adder = new DoubleAdder();
        Runnable runnable = getRunnable(derivative, processSupplier, adder);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < workerThreads; i++) {
            Thread thread = new Thread(runnable);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread: threads) {
            try {
                thread.join();
            } catch(Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        return adder.sum() / workerThreads;
    }

    private Runnable getRunnable(Derivative derivative, Supplier<StochasticProcess> processSupplier, DoubleAdder adder) {

        double dt = derivative.getMaturity() / steps;

        return () -> {
            StochasticProcess process = processSupplier.get();
            double localSum = 0;
            double r = process.drift;
            double scalingFactor = Math.exp(-r * derivative.getMaturity()) / N;

            for(int i = 0; i < N; i++) {
                List<Double> path = process.simulateSteps(steps, dt);
                localSum += derivative.payoff(path);
                if(i % 10 == 0 && (i != 0)) {
                    System.out.println("Current price: " + scalingFactor * localSum * N / i);
                }
                process.reset();
            }

            adder.add(scalingFactor * localSum);
        };
    }
}
