package org.example;

import java.util.List;

public class EuropeanOption extends Option {

    public double strikePrice;
    public double maturity;


    public EuropeanOption(Option.Type optionType,
                          double strikePrice,
                          double maturity
    ) {
        this.type = optionType;
        this.strikePrice = strikePrice;
        this.maturity = maturity;
    }

    @Override
    double callRawPayoff(StochasticProcess process) {
        return Math.max(process.path[process.path.length - 1] - strikePrice, 0);
    }

    @Override
    double putRawPayoff(StochasticProcess process) {
        return Math.max(strikePrice - process.path[process.path.length - 1], 0);
    }

    @Override
    double callPayoffDerivative(StochasticProcess process, int i) {
        if(i == process.path.length - 1) {
            return process.path[i] > strikePrice ? 1 : 0;
        } else return 0;
    }

    @Override
    double putPayoffDerivative(StochasticProcess process, int i) {
        if(i == process.path.length - 1) {
            return process.path[i] > strikePrice ? 1 : 0;
        } else return 0;
    }

    @Override
    public double getMaturity() {
        return maturity;
    }
}
