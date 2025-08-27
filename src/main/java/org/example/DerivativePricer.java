package org.example;

public interface DerivativePricer {
    double getPrice(Derivative derivative, StochasticProcess process);
}
