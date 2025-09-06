package org.example;

import java.util.ArrayList;
import java.util.List;

public abstract class StochasticProcess {

    public double spot;
    public double current;
    public double drift;
    public double volatility;

    public StochasticProcess(double spot, double drift, double volatility) {
        this.spot = spot;
        this.drift = drift;
        this.volatility = volatility;
        current = spot;
    }

    public abstract double simulateStep(double dt, double Z);

    public List<Double> simulateSteps(int count, double dt, double[] randoms) {
        List<Double> list = new ArrayList<>();
        list.add(spot);
        for(int i = 0; i < count; i++) {
            list.add(simulateStep(dt, randoms[i]));
        }
        return list;
    }

    public void reset() {
        current = spot;
    }

}
