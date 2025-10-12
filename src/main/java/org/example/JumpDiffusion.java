package org.example;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.example.GreekData.PathwiseGreeks;
import org.example.GreekData.LogScore;

import java.util.ArrayList;
import java.util.List;

public class JumpDiffusion extends StochasticProcess {
    // The JumpDiffusion stochastic process

    public double lambda;
    public double mu_j;
    public double sigma_j;
    public double driftCorrection;


    public JumpDiffusion(double spot, double drift, double volatility) {
        super(spot, drift, volatility);

        // Temporary: The following parameters will be modifiable later

        lambda = 1;
        mu_j = -0.1;
        sigma_j = 0.2;
        driftCorrection = -lambda * (Math.exp(mu_j + 0.5 * sigma_j * sigma_j) - 1);
    }

    @Override
    public void simulateSteps(int count, double dt, double[] randoms) {
        List<Double> list = new ArrayList<>();
        list.add(spot);

        PoissonDistribution poisson = new PoissonDistribution(lambda * dt);
        NormalDistribution normalDistribution = new NormalDistribution();

        for(int i = 0; i < count; i++) {
            int N = poisson.sample();

            // Calculate sum of normals:
            double J = Math.sqrt(N) * sigma_j * normalDistribution.sample() + N * mu_j;
            list.add(simulateStep(dt, randoms[i], J));
        }
        // TODO: Reimplement
    }

    public double simulateStep(double dt, double Z, double jump) {
        double adjustedDrift = (drift + driftCorrection) - 0.5 * volatility * volatility;
        current *=  Math.exp(adjustedDrift * dt + volatility * Math.sqrt(dt) * Z + jump);
        return current;
    }

    @Override
    public LogScore getLogScore() {
        return null;
    }

    @Override
    public PathwiseGreeks getPathwiseGreeks(Derivative derivative) {
        return null;
    }

    @Override
    public double stepDerivative(int i) throws OutOfRangeException {
        return 0;
    }
}
