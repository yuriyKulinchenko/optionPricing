package org.example;

import java.util.List;

public abstract class Option extends Derivative {

    public Type type;

    public enum Type {
        CALL,
        PUT
    }

    abstract double callRawPayoff(StochasticProcess process);
    abstract double putRawPayoff(StochasticProcess process);

    abstract double callPayoffDerivative(StochasticProcess process, int i);
    abstract double putPayoffDerivative(StochasticProcess process, int i);

    @Override
    double rawPayoff(StochasticProcess process) {
        if(type == Type.CALL) {
            return callRawPayoff(process);
        } else {
            return putRawPayoff(process);
        }
    }

    @Override
    double payoffDerivative(StochasticProcess process, int i) {
        if(type == Type.CALL) {
            return callPayoffDerivative(process, i);
        } else {
            return putPayoffDerivative(process, i);
        }
    }
}
