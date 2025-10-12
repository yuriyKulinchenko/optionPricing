package org.example;

import org.apache.commons.math3.exception.OutOfRangeException;

import java.util.ArrayList;
import java.util.List;
import org.example.Derivative.DerivativePrice;

public abstract class StochasticProcess {

    public static class LogScore {
        public double delta;
        public double rho;
        public double theta;
        public double vega;

        public LogScore(
                double delta,
                double rho,
                double theta,
                double vega
        ) {
            this.delta = delta;
            this.rho = rho;
            this.theta = theta;
            this.vega = vega;
        }
    }

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
    public abstract DerivativePrice payoff(Derivative derivative);

    // stepDerivative(i) := dS_i/dS_i-1
    public abstract double stepDerivative(int i);


    public void reset() {
        current = spot;
    }

}
