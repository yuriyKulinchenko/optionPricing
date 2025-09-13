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

    public static class MTCData {

        public final DoubleAdder adder;
        public final DoubleAdder deltaAdder;

        public final List<List<Vector2D>> samplePathList;
        public final List<List<Double>> sumsList;
        public final List<List<Double>> sumSquaresList;

        public MTCData() {
            this.adder = new DoubleAdder();
            this.deltaAdder = new DoubleAdder();

            this.samplePathList = Collections.synchronizedList(new ArrayList<>());
            this.sumsList = Collections.synchronizedList(new ArrayList<>());
            this.sumSquaresList = Collections.synchronizedList(new ArrayList<>());
        }
    }

    @Override
    public PricerResult getPrice(Derivative derivative, Supplier<StochasticProcess> processSupplier) {

        MTCData data = new MTCData();
        double discountFactor = Math.exp(-rate * derivative.getMaturity());

        Runnable runnable = getRunnable(derivative, processSupplier, data);

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

        List<Double> sums = flatten(data.sumsList)
                .map(x -> discountFactor * x).toList();
        List<Double> sumSquares = flatten(data.sumSquaresList)
                .map(x -> discountFactor * discountFactor * x).toList();

        double constant = discountFactor / (workerThreads * N);

        return new PricerResult(
                data.adder.sum() * constant,
                data.deltaAdder.sum() * constant,
                data.samplePathList,
                sums,
                sumSquares,
                MonteCarloPricerConfig.BATCH_SIZE
        );
    }

    private Runnable getRunnable(Derivative derivative,
                                 Supplier<StochasticProcess> processSupplier,
                                 MTCData data
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
            double deltaSum = 0;

            double batchSum = 0;
            double batchSumSquare = 0;

            for(int i = 0; i < N; i++) {

                double[] randoms = sobol.nextNormalVector();

                process.simulateSteps(steps, dt, randoms);
                Derivative.DerivativePrice payoff = derivative.payoff(process);

                sum += payoff.price;
                deltaSum += payoff.delta;

                batchSum += payoff.price;
                batchSumSquare += payoff.price * payoff.price;


                if(i < 3) {
                    // For now, sample just 3 paths from each thread
                    List<Vector2D> vectorPath = new ArrayList<>();
                    for(int j = 0; j < process.path.length; j++) {
                        vectorPath.add(new Vector2D(j * dt, process.path[j]));
                    }
                    data.samplePathList.add(vectorPath);
                }

                if(((i % MonteCarloPricerConfig.BATCH_SIZE == 0) && i != 0)) {
                    sums.add(batchSum);
                    sumSquares.add(batchSumSquare);
                    batchSumSquare = 0;
                    batchSum = 0;
                }

                process.reset();
            }

            data.sumsList.add(sums);
            data.sumSquaresList.add(sumSquares);
            data.adder.add(sum);
            data.deltaAdder.add(deltaSum);
        };
    }
}
