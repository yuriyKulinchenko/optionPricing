package org.example;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.example.GreekData.LogScore;
import org.example.GreekData.PathwiseGreeks;

public class GeometricBrownianMotion extends StochasticProcess {

    public GeometricBrownianMotion(double spot, double drift, double volatility) {
        super(spot, drift, volatility);
    }

    @Override
    public void simulateSteps(int count, double dt, double[] randoms) {
        this.path = new double[count + 1];
        this.randoms = randoms;
        this.dt = dt;

        path[0] = spot;
        double current = spot;

        double adjustedDrift = drift - 0.5 * volatility * volatility;
        for(int i = 0; i < count; i++) {
            double Z = randoms[i];
            current *= Math.exp(adjustedDrift * dt + volatility * Math.sqrt(dt) * Z);
            path[i + 1] = current;
        }
    }

    // Both payoff and getLogScore rely on this.path and this.randoms being populated

    /*
    TODO: Further generalize StochasticProcess by making this.path and this.randoms GBM specific
    This way, more complex stochastic processes with multiple sources of randomness can be handled
    */

    @Override
    public PathwiseGreeks getPathwiseGreeks(Derivative derivative) {
        // Assume rawPayoff and payoffDerivative are NOT discounted
        int N = this.path.length;
        double[] adjointList = new double[N];
        adjointList[N - 1] = derivative.payoffDerivative(this, N - 1);

        double T = derivative.getMaturity();

        double r = drift;
        double sigma = volatility;

        double alpha_t = (r - 0.5 * sigma * sigma) / N;
        double beta_t = sigma / (2 * Math.sqrt(N * T));

        double alpha_sigma = - volatility * dt;
        double beta_sigma = Math.sqrt(dt);

        double product = adjointList[N - 1] * path[N - 1];
        double productSum = product;
        double productRandomSum = product * randoms[N - 2];

        for(int i = N - 2; i >= 0; i--) {
            adjointList[i] = adjointList[i + 1] * stepDerivative(i + 1)
                    + derivative.payoffDerivative(this, i);

            product = adjointList[i] * path[i];
            productSum += product;
            if(i != 0) productRandomSum += product * randoms[i-1];
        }

        double delta = adjointList[0];
        double rho = productSum * dt;
        double theta = alpha_t * productSum + beta_t * productRandomSum;
        double vega = alpha_sigma * productSum + beta_sigma * productRandomSum;

        return new PathwiseGreeks(delta, rho, theta, vega);
    }

    @Override
    public LogScore getLogScore() {

        double adjustedDrift = drift - 0.5 * volatility * volatility;
        int n = randoms.length;
        double sqrtDt = Math.sqrt(dt);
        double vol = volatility;

        double sumZ = 0;
        double sumSquaredZ = 0;

        for(double Z: randoms) {
            sumZ += Z;
            sumSquaredZ += Z * Z;
        }

        double delta = (Math.log(path[1] / path[0]) - adjustedDrift * dt)
                / (spot * vol * vol * dt);

        double rho = sumZ / (vol * vol);

        double vega = (sumSquaredZ - n) / vol - sqrtDt * sumZ;

        double theta = sumSquaredZ / (2 * dt) - n / (2 * dt)
                + (adjustedDrift * sumZ) / (vol * sqrtDt);

        theta /= n;

        return new LogScore(delta, rho, theta, vega);
    }

    @Override
    public double stepDerivative(int i) throws OutOfRangeException {
        return path[i] / path[i - 1];
    }
}
