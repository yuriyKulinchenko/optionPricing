package org.example;

import java.util.List;

public abstract class Option implements Derivative {

    public Type type;

    public enum Type {
        CALL,
        PUT
    }

    abstract double callPayoff(StochasticProcess process);
    abstract double putPayoff(StochasticProcess process);

    @Override
    public double payoff(StochasticProcess process) {
        if(type == Type.CALL) {
            return callPayoff(process);
        } else {
            return putPayoff(process);
        }
    }
}
