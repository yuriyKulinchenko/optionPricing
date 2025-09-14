package org.example;

public abstract class Derivative {

    public static class DerivativePrice {
        public double price;
        public double delta;
        public double rho;
        public double theta;
        public double vega;

        public DerivativePrice(
                double price,
                double delta,
                double rho,
                double theta,
                double vega
        ) {
            this.price = price;
            this.delta = delta;
            this.rho = rho;
            this.theta = theta;
            this.vega = vega;
        }
    }

    abstract double rawPayoff(StochasticProcess process);
    abstract double payoffDerivative(StochasticProcess process, int i);
    abstract double getMaturity();

    DerivativePrice payoff(StochasticProcess process) {
        // Assume rawPayoff and payoffDerivative are NOT discounted
        int N = process.path.length;
        double[] adjointList = new double[N];
        adjointList[N - 1] = payoffDerivative(process, N - 1);

        double T = getMaturity();
        double dt = process.dt;

        double r = process.drift;
        double sigma = process.volatility;

        double alpha_t = (r - 0.5 * sigma * sigma) / N;
        double beta_t = sigma / (2 * Math.sqrt(N * T));

        double alpha_sigma = - process.volatility * dt;
        double beta_sigma = Math.sqrt(dt);

        double product = adjointList[N - 1] * process.path[N - 1];
        double productSum = product;
        double productRandomSum = product * process.randoms[N - 2];

        for(int i = N - 2; i >= 0; i--) {
            adjointList[i] = adjointList[i + 1] * process.stepDerivative(i + 1)
                    + payoffDerivative(process, i);

            product = adjointList[i] * process.path[i];
            productSum += product;
            if(i != 0) productRandomSum += product * process.randoms[i-1];
        }

        double price = rawPayoff(process);
        double delta = adjointList[0];
        double rho = productSum * dt;
        double theta = alpha_t * productSum + beta_t * productRandomSum;
        double vega = alpha_sigma * productSum + beta_sigma * productRandomSum;


        return new DerivativePrice(price, delta, rho, theta, vega);
    }
}
