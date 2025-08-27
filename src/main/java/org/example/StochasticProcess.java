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

    public abstract double simulateStep(double dt);

    public List<Double> simulateSteps(double count, double dt) {
        double dt2 = dt * dt;
        List<Double> list = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            list.add(simulateStep(dt));
        }
        return list;
    }

    public void reset() {
        current = spot;
    }

}
