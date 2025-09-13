package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public interface Derivative {
    double payoff(StochasticProcess process);
    double getMaturity();
}
