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
    double callRawPayoff(StochasticProcess process) {
        double avg = (averagingType == AveragingType.ARITHMETIC) ?
                arithmeticAverage(process.path) : geometricAverage(process.path);

        return Math.max(avg - strikePrice, 0);
    }

    @Override
    double putRawPayoff(StochasticProcess process) {
        double avg = (averagingType == AveragingType.ARITHMETIC) ?
                arithmeticAverage(process.path) : geometricAverage(process.path);

        return Math.max(strikePrice - avg, 0);
    }

    @Override
    double callPayoffDerivative(StochasticProcess process, int i) {
        return 0;
    }

    @Override
    double putPayoffDerivative(StochasticProcess process, int i) {
        return 0;
    }

    @Override
    double payoffDerivative(StochasticProcess process, int i) {
        return 0;
    }

    @Override
    public double getMaturity() {
        return maturity;
    }
}
