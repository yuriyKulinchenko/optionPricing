package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeometricBrownianBridge extends StochasticProcess {

    private double[] randoms;
    private int index;

    public GeometricBrownianBridge(double spot, double drift, double volatility) {
        super(spot, drift, volatility);
    }

    @Override
    public List<Double> simulateSteps(int count, double dt, double[] randoms) {
        this.randoms = randoms;
        this.index = 0;

        List<Double> process = new ArrayList<>(Collections.nCopies(randoms.length, null));
        double variance = (randoms.length - 1) * dt;
        process.set(0, 0.);
        process.set(randoms.length - 1, getNormal(0, variance));
        simulateWienerProcess(process, 0, randoms.length - 1);
        return process;
    }

    public void simulateWienerProcess(List<Double> process, int i, int k) {
        if(i == k) return;
        int j = (i + k) / 2;
        if((i == j) || (j == k)) return;

        double variance = (double) ((j - i) * (k - j)) / (k - i);
        double mean = ((k - j) * process.get(k) + (j - i) * process.get(i)) / (k - i);
        process.set(j, getNormal(mean, variance));

        simulateWienerProcess(process, i, j);
        simulateWienerProcess(process, j, k);
    }

    private double getNormal() {
        return randoms[index++];
    }

    private double getNormal(double mean, double variance) {
        return Math.sqrt(variance) * getNormal() + mean;
    }

    @Override
    public double simulateStep(double dt, double Z) {
        // Should not be used: Defeats purpose of bridge construction
        double adjustedDrift = drift - 0.5 * volatility * volatility;
        current *=  Math.exp(adjustedDrift * dt + volatility * Math.sqrt(dt) * Z);
        return current;
    }
}
