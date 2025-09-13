package org.example;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;
import java.util.function.Supplier;

public interface DerivativePricer {

    class PricerResult {

        public double derivativePrice;
        public double delta;
        public double rho;
        public List<List<Vector2D>> paths;

        public List<Double> sums;
        public List<Double> squares;

        public int chunkSize;

        public PricerResult(
                double derivativePrice,
                double delta,
                double rho,
                List<List<Vector2D>> paths,
                List<Double> sums,
                List<Double> squares,
                int chunkSize

        ) {
            this.derivativePrice = derivativePrice;
            this.delta = delta;
            this.rho = rho;
            this.paths = paths;
            this.sums = sums;
            this.squares = squares;
            this.chunkSize = chunkSize;
        }
    }

    PricerResult getPrice(Derivative derivative, Supplier<StochasticProcess> processSupplier);
}
