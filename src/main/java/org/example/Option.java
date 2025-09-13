package org.example;

import java.util.List;

public abstract class Option implements Derivative {

    public Type type;

    public enum Type {
        CALL,
        PUT
    }

    abstract DerivativePrice callPayoff(StochasticProcess process);
    abstract DerivativePrice putPayoff(StochasticProcess process);

    @Override
    public DerivativePrice payoff(StochasticProcess process) {
        if(type == Type.CALL) {
            return callPayoff(process);
        } else {
            return putPayoff(process);
        }
    }
}
