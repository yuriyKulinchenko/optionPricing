package org.example;

import org.apache.commons.math3.exception.OutOfRangeException;

public class Heston extends StochasticProcess {

    public double volOfVol;
    public double correlation;
    public double correlationPair;
    public double reversionSpeed;
    public double meanVariance;

    public double[] varPath;
    public double[] X1List;
    public double[] X2List;

    public Heston(double spot, double drift, double volatility) {
        super(spot, drift, volatility);
        this.volOfVol = UniversalData.volOfVol;
        this.correlation = UniversalData.correlation;
        this.correlationPair = Math.sqrt(1 - correlation * correlation);
        this.meanVariance = volatility * volatility;
        this.reversionSpeed = UniversalData.reversionSpeed;
    }

    public Heston(
            double spot,
            double drift,
            double volatility,
            double volOfVol,
            double correlation,
            double reversionSpeed
    ) {
        super(spot, drift, volatility);
        this.volOfVol = volOfVol;
        this.correlation = correlation;
        this.correlationPair = Math.sqrt(1 - correlation * correlation);
        this.meanVariance = volatility * volatility;
        this.reversionSpeed = reversionSpeed;
    }

    public double getZ1(int i) {
        return randoms[2 * i];
    }

    public double getZ2(int i) {
        return randoms[2 * i + 1];
    }

    @Override
    public void simulateSteps(int count, double dt, double[] randoms) {
        this.path = new double[count + 1];
        this.varPath = new double[count + 1];
        this.X1List = new double[randoms.length / 2];
        this.X2List = new double[randoms.length / 2];
        this.randoms = randoms;
        this.dt = dt;

        double current = spot;
        double varCurrent = meanVariance;

        path[0] = current;
        varPath[0] = varCurrent;

        double sqrtDt = Math.sqrt(dt);

        for(int i = 0; i < count; i++) {
            double X1 = getZ1(i);
            double X2 = correlation * X1 + correlationPair * getZ2(i);

            X1List[i] = X1;
            X2List[i] = X2;
            // First, compute change to variance process

            varCurrent += reversionSpeed * (meanVariance - varCurrent) * dt
                    + volOfVol * Math.sqrt(varCurrent * dt) * X2;

            if(varCurrent < 0) varCurrent = 0;

            // Then, compute change to stochastic process

            double adjustedDrift = drift - 0.5 * varCurrent;
            double volCurrent = Math.sqrt(varCurrent);

            current *= Math.exp(adjustedDrift * dt + volCurrent * sqrtDt * X1);
            path[i + 1] = current;
            varPath[i + 1] = varCurrent;
        }
    }

    @Override
    public GreekData.LogScore getLogScore() {
        return new GreekData.LogScore(0, 0, 0, 0);
    }

    @Override
    public GreekData.PathwiseGreeks getPathwiseGreeks(Derivative derivative) {
        int N = this.path.length;

        double[] pathAdjointList = new double[N];
        double[] varAdjointList = new double[N];

        pathAdjointList[N - 1] = derivative.payoffDerivative(this, N - 1);
        varAdjointList[N - 1] = pathAdjointList[N-1] * pathVarianceDerivative(N - 1);

        double pathProduct = pathAdjointList[N - 1] * path[N - 1];
        double pathProductSum = pathProduct;

        double varSum = varAdjointList[N - 1];

        for(int i = N - 2; i >= 0; i--) {
            pathAdjointList[i] = pathAdjointList[i + 1] * stepDerivative(i + 1)
                    + derivative.payoffDerivative(this, i);

            varAdjointList[i] = varAdjointList[i + 1] * stepDerivativeVariance(i + 1)
                    + pathAdjointList[i] * pathVarianceDerivative(i);

            pathProduct = pathAdjointList[i] * path[i];

            pathProductSum += pathProduct;
            varSum += varAdjointList[i];
        }

        double delta = pathAdjointList[0];
        double rho = pathProductSum * dt;
        double theta = 0;
        double vega = 2 * Math.sqrt(meanVariance) * (dt * reversionSpeed * varSum  + varAdjointList[0]);

        return new GreekData.PathwiseGreeks(delta, rho, theta, vega);
    }

    @Override
    public double stepDerivative(int i) throws OutOfRangeException {
        return path[i] / path[i - 1];
    }

    public double stepDerivativeVariance(int i) throws OutOfRangeException {
        return 1 - reversionSpeed * dt +
                0.5 * (volOfVol * X2List[i - 1]) * Math.sqrt(dt/varPath[i - 1]);
    }

    public double pathVarianceDerivative(int i) {
        // returns dS_i/dV_i
        if(i == 0) return 0;
        return path[i] * (0.5 * Math.sqrt(dt / varPath[i]) * X1List[i - 1] - 0.5 * dt);
    }

    @Override
    public int getRandomsPerStep() {
        return 2;
    }
}
