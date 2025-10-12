package org.example;

import org.apache.commons.math3.exception.OutOfRangeException;

import java.util.ArrayList;
import java.util.List;
import org.example.Derivative.DerivativePrice;
import org.example.Derivative.LogScore;
import org.example.Derivative.PathwiseGreeks;

public abstract class StochasticProcess {

    public double spot;
    public double current;
    public double drift;
    public double volatility;

    public double[] path;
    public double[] randoms;

    public double dt;

    public StochasticProcess(double spot, double drift, double volatility) {
        this.spot = spot;
        this.drift = drift;
        this.volatility = volatility;
        current = spot;
    }

    public abstract void simulateSteps(int count, double dt, double[] randoms);

    public abstract LogScore getLogScore();
    public abstract PathwiseGreeks getPathwiseGreeks(Derivative derivative);


    // stepDerivative(i) := dS_i/dS_i-1
    public abstract double stepDerivative(int i);


    public void reset() {
        current = spot;
    }

}
