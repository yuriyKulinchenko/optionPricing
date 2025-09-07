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
    double callPayoff(List<Double> path) {
        return Math.max(path.getLast() - strikePrice, 0);
    }

    @Override
    double putPayoff(List<Double> path) {
        return Math.max(strikePrice - path.getLast(), 0);
    }

    @Override
    public double getMaturity() {
        return maturity;
    }
}
