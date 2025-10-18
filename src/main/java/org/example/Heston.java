package org.example;

public class Heston extends StochasticProcess {

    public double volOfVol;
    public double correlation;
    public double correlationPair;
    public double reversionSpeed;
    public double meanVariance;
    public double[] varPath;

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
        this.reversionSpeed = reversionSpeed * 100;
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
        this.randoms = randoms;
        this.dt = dt;

        double current = spot;
        double varCurrent = meanVariance;

        path[0] = current;
        varPath[0] = varCurrent;

        double sqrtDt = Math.sqrt(dt);

        for(int i = 0; i < count; i++) {
            double Z1 = getZ1(i);
            double Z2 = getZ2(i);

            // First, compute change to variance process

            varCurrent += reversionSpeed * (meanVariance - varCurrent) * dt
                    + volOfVol * Math.sqrt(varCurrent * dt) * Z1;

            if(varCurrent < 0) varCurrent = 0;

            // Then, compute change to stochastic process

            double adjustedDrift = drift - 0.5 * varCurrent;
            current *= Math.exp(adjustedDrift * dt + volatility * sqrtDt * Z2);
            path[i + 1] = current;
        }
    }

    @Override
    public GreekData.LogScore getLogScore() {
        return new GreekData.LogScore(0, 0, 0, 0);
    }

    @Override
    public GreekData.PathwiseGreeks getPathwiseGreeks(Derivative derivative) {
        return new GreekData.PathwiseGreeks(0, 0, 0, 0);
    }

    @Override
    public double stepDerivative(int i) {
        return 0;
    }

    @Override
    public int getRandomsPerStep() {
        return 2;
    }
}
