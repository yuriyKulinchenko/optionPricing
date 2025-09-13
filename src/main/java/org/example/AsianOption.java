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
    public double callPayoff(StochasticProcess process) {

        double avg = (averagingType == AveragingType.ARITHMETIC) ?
                arithmeticAverage(process.process) : geometricAverage(process.process);

        return Math.max(avg - strikePrice, 0);
    }

    @Override
    double putPayoff(StochasticProcess process) {
        double avg = (averagingType == AveragingType.ARITHMETIC) ?
                arithmeticAverage(process.process) : geometricAverage(process.process);

        return Math.max(strikePrice - avg, 0);
    }

    @Override
    public double getMaturity() {
        return maturity;
    }
}
