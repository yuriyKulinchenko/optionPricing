package org.example;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;
import java.util.function.Supplier;

public interface DerivativePricer {

    public static class PricerResult {
        public double derivativePrice;
        public List<List<Vector2D>> paths;

        public PricerResult(double derivativePrice, List<List<Vector2D>> paths) {
            this.derivativePrice = derivativePrice;
            this.paths = paths;
        }
    }

    PricerResult getPrice(Derivative derivative, Supplier<StochasticProcess> processSupplier);
}
