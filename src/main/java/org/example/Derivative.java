package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public interface Derivative {
    // Maturity is assumed to be at the end of 'path'
    public double payoff(List<Double> path);
    public void addChart(XYChart chart);
}
