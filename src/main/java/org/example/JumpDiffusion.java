package org.example;

public class JumpDiffusion extends StochasticProcess {
    public JumpDiffusion(double spot, double drift, double volatility) {
        super(spot, drift, volatility);
    }

    @Override
    public double simulateStep(double dt, double Z) {
        return 0;
    }
}
