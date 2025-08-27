package org.example;

public class GeometricBrownianMotion extends StochasticProcess {

    public GeometricBrownianMotion(double spot, double drift, double volatility) {
        super(spot, drift, volatility);
    }

    @Override
    public double simulateStep(double dt) {
        double Z = RandomGenerator.getInstance().nextGaussian();
        double adjustedDrift = drift - 0.5 * volatility * volatility;
        current *=  Math.exp(adjustedDrift * dt + volatility * Math.sqrt(dt) * Z);
        return current;
    }
}
