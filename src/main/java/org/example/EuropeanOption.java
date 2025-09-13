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
    DerivativePrice callPayoff(StochasticProcess process) {
        double price = Math.max(process.process[process.process.length - 1] - strikePrice, 0);
        return new DerivativePrice(price, 0);
    }

    @Override
    DerivativePrice putPayoff(StochasticProcess process) {
        double price = Math.max(strikePrice - process.process[process.process.length - 1], 0);
        return new DerivativePrice(price, 0);
    }

    @Override
    public double getMaturity() {
        return maturity;
    }
}
