package org.example;
import java.util.List;

public class AsianOption extends Option {

    public double strikePrice;
    public double maturity;
    public AveragingType averagingType;

    public enum AveragingType {
        ARITHMETIC,
        GEOMETRIC
    }

    public AsianOption(Option.Type optionType,
                       double strikePrice,
                       double maturity,
                       AveragingType averagingType
    ) {
        this.strikePrice = strikePrice;
        this.maturity = maturity;
        this.averagingType = averagingType;
        this.type = optionType;
    }

    private double arithmeticAverage(double[] path) {
        double sum = 0;
        for(double x: path) {
            sum += x;
        }
        return sum / path.length;
    }

    private double geometricAverage(double[] path) {
        double sum = 0;
        for(double x: path) {
            sum += Math.log(x);
        }
        return Math.exp(sum / path.length);
    }

    @Override
    public DerivativePrice callPayoff(StochasticProcess process) {

        double avg = (averagingType == AveragingType.ARITHMETIC) ?
                arithmeticAverage(process.process) : geometricAverage(process.process);

        return new DerivativePrice(Math.max(avg - strikePrice, 0), 0);
    }

    @Override
    DerivativePrice putPayoff(StochasticProcess process) {
        double avg = (averagingType == AveragingType.ARITHMETIC) ?
                arithmeticAverage(process.process) : geometricAverage(process.process);

        return new DerivativePrice(Math.max(strikePrice - avg, 0), 0);
    }

    @Override
    public double getMaturity() {
        return maturity;
    }
}
