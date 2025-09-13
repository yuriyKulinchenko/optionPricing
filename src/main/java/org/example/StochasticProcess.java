package org.example;

import org.apache.commons.math3.exception.OutOfRangeException;

import java.util.ArrayList;
import java.util.List;

public abstract class StochasticProcess {

    public double spot;
    public double current;
    public double drift;
    public double volatility;

    public double[] process;
    public double dt;

    public StochasticProcess(double spot, double drift, double volatility) {
        this.spot = spot;
        this.drift = drift;
        this.volatility = volatility;
        current = spot;
    }

    public abstract double simulateStep(double dt, double Z);

    // stepDerivative(i) := dS_i/dS_i-1
    public abstract double stepDerivative(int i);

    public void simulateSteps(int count, double dt, double[] randoms) {
        if(process == null || process.length != count) {
            process = new double[count + 1];
        }

        process[0] = spot;
        this.dt = dt;

        for(int i = 0; i < count; i++) {
            process[i + 1] = simulateStep(dt, randoms[i]);
        }
    }

    public void reset() {
        current = spot;
    }

}
