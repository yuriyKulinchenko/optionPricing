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

    private double arithmeticAverage(List<Double> path) {
        double sum = 0;
        for(double x: path) {
            sum += x;
        }
        return sum / path.size();
    }

    private double geometricAverage(List<Double> path) {
        double sum = 0;
        for(double x: path) {
            sum += Math.log(x);
        }
        return Math.exp(sum / path.size());
    }

    @Override
    public double callPayoff(List<Double> path) {

        double avg = (averagingType == AveragingType.ARITHMETIC) ?
                arithmeticAverage(path) : geometricAverage(path);

        return Math.max(avg - strikePrice, 0);
    }

    @Override
    double putPayoff(List<Double> path) {
        double avg = (averagingType == AveragingType.ARITHMETIC) ?
                arithmeticAverage(path) : geometricAverage(path);

        return Math.max(strikePrice - avg, 0);
    }

    @Override
    public double getMaturity() {
        return maturity;
    }
}
