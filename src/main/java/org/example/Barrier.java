package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class Barrier implements Derivative {

    private final Derivative derivative;
    private final double barrier;
    private final boolean knockIn;

    private boolean isUp;

    public Barrier(Derivative derivative, double barrier, boolean knockIn) {
        this.derivative = derivative;
        this.barrier = barrier;
        this.knockIn = knockIn;
    }

    @Override
    public double payoff(List<Double> path) {
        isUp = path.getFirst() < barrier;

        boolean breached = breachedBarrier(path.getFirst()) || breachedBarrier(path.getLast());
        if(!breached) breached = breachedBarrier(path);

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

    @Override
    public void addChart(XYChart chart) {
        // TODO: Implement chart
    }
}
