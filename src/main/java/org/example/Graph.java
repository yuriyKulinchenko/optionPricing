package org.example;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

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
                .title("Asset price")
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
        XYSeries series = chart.addSeries(name, xList, yList);
        series.setMarker(SeriesMarkers.NONE);
        series.setSmooth(true);
    }

    public void addSeries(String name, List<Double> yList) {
        List<Double> xList = new ArrayList<>();
        for (int i = 0; i < yList.size(); i++) {
            xList.add((double)i);
        }
        addSeries(name, xList, yList);
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
