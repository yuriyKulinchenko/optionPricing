package org.example;

import java.util.concurrent.atomic.DoubleAdder;

public class GreekData {
    public double delta;
    public double rho;
    public double theta;
    public double vega;

    public GreekData(double delta, double rho, double theta, double vega) {
        this.delta = delta;
        this.rho = rho;
        this.theta = theta;
        this.vega = vega;
    }

    // All addition and multiplication mutates state

    public GreekData add(double delta, double rho, double theta, double vega) {
        this.delta += delta;
        this.rho += rho;
        this.theta += theta;
        this.vega += vega;
        return this;
    }

    public GreekData add(GreekData other) {
        delta += other.delta;
        rho += other.rho;
        theta += other.theta;
        vega += other.vega;
        return this;
    }

    @Override
    public String toString() {
        return "GreekData{" +
                "delta=" + delta +
                ", rho=" + rho +
                ", theta=" + theta +
                ", vega=" + vega +
                '}';
    }

    public GreekData multiply(double constant) {
        this.delta *= constant;
        this.rho *= constant;
        this.theta *= constant;
        this.vega *= constant;
        return this;
    }

    public static class PathwiseGreeks extends GreekData {
        public PathwiseGreeks(double delta, double rho, double theta, double vega) {
            super(delta, rho, theta, vega);
        }
    }

    public static class LogScore extends GreekData {
        public LogScore(double delta, double rho, double theta, double vega) {
            super(delta, rho, theta, vega);
        }
    }

    public static class GreekDataAdder {
        public final DoubleAdder deltaAdder = new DoubleAdder();
        public final DoubleAdder rhoAdder = new DoubleAdder();
        public final DoubleAdder thetaAdder = new DoubleAdder();
        public final DoubleAdder vegaAdder = new DoubleAdder();

        public void add(GreekData data) {
            deltaAdder.add(data.delta);
            rhoAdder.add(data.rho);
            thetaAdder.add(data.theta);
            vegaAdder.add(data.vega);
        }

        public GreekData sum() {
            return new GreekData(deltaAdder.sum(), rhoAdder.sum(), thetaAdder.sum(), vegaAdder.sum());
        }
    }
}
