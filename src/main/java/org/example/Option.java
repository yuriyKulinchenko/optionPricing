package org.example;

import java.util.List;

public abstract class Option implements Derivative {

    public Type type;

    public static enum Type {
        CALL,
        PUT
    }

    abstract double callPayoff(List<Double> path);
    abstract double putPayoff(List<Double> path);

    @Override
    public double payoff(List<Double> path) {
        if(type == Type.CALL) {
            return callPayoff(path);
        } else {
            return putPayoff(path);
        }
    }
}
