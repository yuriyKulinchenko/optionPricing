package org.example;

import java.util.function.Supplier;

public interface DerivativePricer {
    double getPrice(Derivative derivative, Supplier<StochasticProcess> processSupplier);
}
