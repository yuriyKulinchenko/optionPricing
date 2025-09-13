package org.example;

import java.util.ArrayList;
import java.util.List;

public class GeometricBrownianBridge extends StochasticProcess {

    private double[] randoms;
    private int index;
    private double dt;

    public GeometricBrownianBridge(double spot, double drift, double volatility) {
        super(spot, drift, volatility);
    }

    @Override
    public void simulateSteps(int count, double dt, double[] randoms) {
        this.randoms = randoms;
        this.index = 0;
        this.dt = dt;

        double[] process = new double[randoms.length];
        double variance = (process.length - 1) * dt;
        process[0] = 0;
        process[process.length - 1] = getNormal(0, variance);
        simulateWienerProcess(process, 0, process.length - 1);

        List<Double> prices = new ArrayList<>(process.length);

        for(int i = 0; i < process.length; i++) {
            double adjustedDrift = drift - 0.5 * volatility * volatility;
            double t = i * dt;
            prices.add(spot * Math.exp(adjustedDrift * t + volatility * process[i]));
        }

        // Does nothing for now
    }

    public void simulateWienerProcess(double[] process, int i, int k) {
        if(i == k) return;
        int j = (i + k) / 2;
        if((i == j) || (j == k)) return;

        double variance = (dt * (j - i) * (k - j)) / (k - i);
        double mean = ((k - j) * process[i] + (j - i) * process[k]) / (k - i);
        process[j] = getNormal(mean, variance);

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

    @Override
    public double stepDerivative(int i) {
        return 0;
    }
}
