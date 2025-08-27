package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public interface Derivative {
    double payoff(List<Double> path);
    double getMaturity();
    void addChart(XYChart chart);
}
