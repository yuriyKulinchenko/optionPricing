package org.example;

import org.knowm.xchart.XYChart;

import java.util.List;

public class AsianOption extends Option {

    public double strikePrice;
    public double maturity;
    public AveragingType averagingType;
    public  Option.Type optionType;

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
        this.optionType = optionType;
    }

    @Override
    public double callPayoff(List<Double> path) {
        double sum = 0;
        for(double x: path) {
            sum += x;
        }
        return Math.max(strikePrice - sum / path.size(), 0);
    }

    @Override
    double putPayoff(List<Double> path) {
        double sum = 0;
        for(double x: path) {
            sum += x;
        }
        return Math.max(sum / path.size() - strikePrice, 0);
    }

    @Override
    public double getMaturity() {
        return maturity;
    }
}
