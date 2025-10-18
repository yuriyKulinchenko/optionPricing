package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class Barrier extends Derivative {

    public final Derivative derivative;
    public final double barrier;
    public final boolean knockIn;
    public final boolean isUp;

    public enum Type {
        UP_IN,
        UP_OUT,
        DOWN_IN,
        DOWN_OUT
    }

    public Barrier(Derivative derivative, double barrier, Type type) {
        this.derivative = derivative;
        this.barrier = barrier;
        this.knockIn = (type == Type.UP_IN) || (type == Type.DOWN_IN);
        this.isUp = (type == Type.UP_IN) || (type == Type.UP_OUT);
    }

    @Override
    double rawPayoff(StochasticProcess process) {
        boolean breached = breachedBarrier(process.path);

        boolean exists = knockIn && breached || !knockIn && !breached;
        return exists ? derivative.rawPayoff(process) : 0;
    }

    @Override
    DerivativePrice payoff(StochasticProcess process) {
        return new DerivativePrice(rawPayoff(process),null, process.getLogScore());
    }

    @Override
    double payoffDerivative(StochasticProcess process, int i) {
        return 0;
    }

    private boolean breachedBarrier(double[] path) {
        for(double x: path) {
            if(breachedBarrier(x)) {
                return true;
            }
        }
        return false;
    }

    private boolean breachedBarrier(double x) {
        if(isUp) {
            return x > barrier;
        } else {
            return x < barrier;
        }
    }


    @Override
    public double getMaturity() {
        return derivative.getMaturity();
    }
}
