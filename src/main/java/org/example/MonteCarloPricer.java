package org.example;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MonteCarloPricer implements DerivativePricer {

    int N; // N is per thread
    int steps;
    int workerThreads;
    double rate;

    public static class MonteCarloPricerConfig {
        public static final boolean DEBUG = false;
        public static final int BATCH_SIZE = 200;
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

    public static <T> Stream<T> flatten(List<List<T>> lists) {
        return lists.stream().flatMap(Collection::stream);
    }

    @Override
    public PricerResult getPrice(Derivative derivative, Supplier<StochasticProcess> processSupplier) {

        DoubleAdder adder = new DoubleAdder();
        List<List<Vector2D>> samplePathList = Collections.synchronizedList(new ArrayList<>());

        List<List<Double>> sumsList = Collections.synchronizedList(new ArrayList<>());
        List<List<Double>> sumSquaresList = Collections.synchronizedList(new ArrayList<>());

        Runnable runnable = getRunnable(
                derivative,
                processSupplier,
                adder,
                samplePathList,
                sumsList,
                sumSquaresList
        );

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

        List<Double> sums = flatten(sumsList)
                .map(x -> scalingFactor * x).toList();
        List<Double> sumSquares = flatten(sumSquaresList)
                .map(x -> scalingFactor * scalingFactor * x).toList();

        return new PricerResult(
                (scalingFactor * adder.sum()) / (workerThreads * N),
                samplePathList,
                sums,
                sumSquares,
                MonteCarloPricerConfig.BATCH_SIZE
        );
    }

    private Runnable getRunnable(Derivative derivative,
                                 Supplier<StochasticProcess> processSupplier,
                                 DoubleAdder adder,
                                 List<List<Vector2D>> samplePathList,
                                 List<List<Double>> sumsList,
                                 List<List<Double>> sumSquaresList
    ) {

        double dt = derivative.getMaturity() / steps;

        return () -> {

            StochasticProcess process = processSupplier.get();

            List<Double> sums = new ArrayList<>();
            List<Double> sumSquares = new ArrayList<>();

            Sobol sobol = new Sobol(steps, N);

            if(process.drift != rate) {
                throw new RuntimeException("Provided StochasticProcess does not have risk free rate specified by pricer");
            }

            double sum = 0;
            double batchSum = 0;
            double batchSumSquare = 0;

            for(int i = 0; i < N; i++) {

                double[] randoms = sobol.nextNormalVector();

                List<Double> path = process.simulateSteps(steps, dt, randoms);
                process.reset();


                double payoff = derivative.payoff(path);

                sum += payoff;
                batchSum += payoff;
                batchSumSquare += payoff * payoff;


                if(i < 3) {
                    // For now, sample just 3 paths from each thread
                    List<Vector2D> vectorPath = new ArrayList<>();
                    for(int j = 0; j < path.size(); j++) {
                        vectorPath.add(new Vector2D(j * dt, path.get(j)));
                    }
                    samplePathList.add(vectorPath);
                }

                if(((i % MonteCarloPricerConfig.BATCH_SIZE == 0) && i != 0)) {
                    sums.add(batchSum);
                    sumSquares.add(batchSumSquare);
                    batchSumSquare = 0;
                    batchSum = 0;
                }
            }

            sumsList.add(sums);
            sumSquaresList.add(sumSquares);
            adder.add(sum);
        };
    }
}
