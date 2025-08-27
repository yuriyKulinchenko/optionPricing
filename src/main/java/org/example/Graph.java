package org.example;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Graph {

    private static Graph singleton;
    private static int plotCount = 0;
    private final XYChart chart;

    private Graph() {
        chart = new XYChartBuilder()
                .width(800).height(600)
                .title("Test graph")
                .xAxisTitle("X axis")
                .yAxisTitle("Y axis")
                .build();
    }

    public static Graph getInstance() {
        if(singleton == null) {
            singleton = new Graph();
        }
        return singleton;
    }

    public void addSeries(String name, List<Double> xList, List<Double> yList) {
        plotCount++;
        chart.addSeries(name, xList, yList);
    }

    public void draw() {
        new SwingWrapper<>(chart).displayChart();
    }

    public void addFunction(double xMin, double xMax, int count, Function<Double, Double> f) {
        double dx = (xMax - xMin) / count;
        ArrayList<Double> xList = new ArrayList<>();
        ArrayList<Double> yList = new ArrayList<>();
        for(double x = xMin; x < xMax; x += dx) {
            xList.add(x);
            yList.add(f.apply(x));
        }
        addSeries("plot " + plotCount, xList, yList);
    }


}
