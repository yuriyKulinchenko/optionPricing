package org.example;

import org.apache.commons.math3.exception.OutOfRangeException;

public class GeometricBrownianMotion extends StochasticProcess {

    public GeometricBrownianMotion(double spot, double drift, double volatility) {
        super(spot, drift, volatility);
    }

    @Override
    public double simulateStep(double dt, double Z) {
        double adjustedDrift = drift - 0.5 * volatility * volatility;
        current *=  Math.exp(adjustedDrift * dt + volatility * Math.sqrt(dt) * Z);
        return current;
    }

    @Override
    public double stepDerivative(int i) throws OutOfRangeException {
        return process[i] / process[i - 1];
    }
}
