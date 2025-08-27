package org.example;

public class GeometricBrownianMotion extends StochasticProcess {

    public GeometricBrownianMotion(double spot, double drift, double volatility) {
        super(spot, drift, volatility);
    }

    @Override
    public double simulateStep(double dt) {
        double Z = RandomGenerator.getInstance().nextGaussian();
        current += drift * dt + volatility * Math.sqrt(dt) * Z;
        return current;
    }
}
