package org.example;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.example.Derivative.DerivativePrice;

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
    public DerivativePrice payoff(Derivative derivative) {
        // Assume rawPayoff and payoffDerivative are NOT discounted
        // TODO: payoff should be associated with stochastic process, not with Derivative
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

        double price = derivative.rawPayoff(this);
        double delta = adjointList[0];
        double rho = productSum * dt;
        double theta = alpha_t * productSum + beta_t * productRandomSum;
        double vega = alpha_sigma * productSum + beta_sigma * productRandomSum;


        return new DerivativePrice(price, delta, rho, theta, vega);
    }

    @Override
    public LogScore getLogScore() {
        return null;
    }

    @Override
    public double stepDerivative(int i) throws OutOfRangeException {
        return path[i] / path[i - 1];
    }
}
