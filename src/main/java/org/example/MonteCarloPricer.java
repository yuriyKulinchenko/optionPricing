package org.example;

import org.apache.commons.math3.random.SobolSequenceGenerator;

import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;

public class MonteCarloPricer implements DerivativePricer {

    int N; // N is per thread
    int steps;
    int workerThreads;
    double rate;

    public static class MonteCarloPricerConfig {
        public static boolean DEBUG = true;
        public static int BATCH_SIZE = 200;
    }

    public static class Builder implements  BuilderInterface<MonteCarloPricer> {

        int N;
        int steps;
        int workerThreads;
        double rate;

        public Builder() {
            N = 1000;
            steps = 100;
            workerThreads = Runtime.getRuntime().availableProcessors() / 2;
            rate = UniversalData.riskFreeRate;
        }

        Builder setIterationCount(int N) {
            this.N = N;
            return this;
        }

        Builder setSteps(int steps) {
            this.steps = steps;
            return this;
        }

        Builder setRate(double rate) {
            this.rate = rate;
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
        this.rate = builder.rate;
    }

    @Override
    public double getPrice(Derivative derivative, Supplier<StochasticProcess> processSupplier) {

        DoubleAdder adder = new DoubleAdder();
        List<List<Double>> samplePricesList = Collections.synchronizedList(new ArrayList<>());
        Runnable runnable = getRunnable(derivative, processSupplier, adder, samplePricesList);

        double scalingFactor = Math.exp(-rate * derivative.getMaturity());

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

        // DEBUG
        if(MonteCarloPricerConfig.DEBUG) {
            List<Double> samplePrices = samplePricesList.stream()
                    .flatMap(Collection::stream)
                    .map(x -> scalingFactor * x)
                    .toList();
            System.out.println("Number of prices sampled: " + samplePrices.size());
            Graph.drawDistribution(samplePrices, 100);
        }

        return (scalingFactor * adder.sum()) / (workerThreads * N);
    }

    private Runnable getRunnable(Derivative derivative,
                                 Supplier<StochasticProcess> processSupplier,
                                 DoubleAdder adder,
                                 List<List<Double>> samplePricesList
    ) {

        double dt = derivative.getMaturity() / steps;

        return () -> {

            StochasticProcess process = processSupplier.get();
            List<Double> samplePrices = new ArrayList<>();
            Sobol sobol = new Sobol(steps, N);

            if(process.drift != rate) {
                throw new RuntimeException("Provided StochasticProcess does not have risk free rate specified by pricer");
            }

            double sum = 0;
            double batchSum = 0;

            for(int i = 0; i < N; i++) {

                double[] randoms = sobol.nextNormalVector();

                List<Double> path = process.simulateSteps(steps, dt, randoms);
                process.reset();

                double payoff = derivative.payoff(path);

                sum += payoff;

                // DEBUG
                if(MonteCarloPricerConfig.DEBUG) {
                    batchSum += payoff;
                    if((i % MonteCarloPricerConfig.BATCH_SIZE) == 0 && (i != 0)) {
                        samplePrices.add(batchSum / MonteCarloPricerConfig.BATCH_SIZE);
                        batchSum = 0;
                    }
                }
            }

            adder.add(sum);

            if(MonteCarloPricerConfig.DEBUG) {
                samplePricesList.add(samplePrices);
            }
        };
    }
}
