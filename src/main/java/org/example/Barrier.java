package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class Barrier implements Derivative {

    private final Derivative derivative;
    private final double barrier;
    private final boolean knockIn;
    private final boolean isUp;

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
    public double payoff(List<Double> path) {
        boolean breached = breachedBarrier(path);

        boolean exists = knockIn && breached || !knockIn && !breached;
        return exists ? derivative.payoff(path) : 0;
    }

    private boolean breachedBarrier(List<Double> path) {
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
