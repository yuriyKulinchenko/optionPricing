package org.example;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();
        int xCount = 1000;
        double xMin = 0;
        double xMax = 2 * Math.PI;
        double dx = (xMax - xMin) / xCount;
        Function<Double, Double> f = Math::sin;
        for (double x = xMin; x < xMax; x+=dx) {
            xList.add(x);
            yList.add(f.apply(x));
        }

        XYChart chart = new XYChartBuilder()
                .width(800).height(600)
                .title("Test graph")
                .xAxisTitle("X axis")
                .yAxisTitle("Y axis")
                .build();

        chart.addSeries("GBM Path", xList, yList);

        new SwingWrapper<>(chart).displayChart();
    }
}