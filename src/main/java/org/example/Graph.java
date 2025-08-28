package org.example;

import org.knowm.xchart.*;
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
    }

    public void addSeries(String name, List<Double> yList, double xMin, double dx) {
        List<Double> xList = new ArrayList<>();
        for (int i = 0; i < yList.size(); i++) {
            xList.add(xMin + i * dx);
        }
        addSeries(name, xList, yList);
    }

    public void addDerivative(Derivative derivative) {
        derivative.addChart(chart);
    }

    public void draw() {
        new SwingWrapper<>(chart).displayChart();
    }

    public static void drawDistribution(List<Double> list, int bucketCount) {
        List<String> xList = new ArrayList<>(bucketCount);
        List<Integer> yList = new ArrayList<>(bucketCount);

        // First, range needs to be determined:

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for(int i = 0; i < list.size(); i++) {
            min = Math.min(min, list.get(i));
            max = Math.max(max, list.get(i));
        }

        // Buckets need to be set up:

        double range = max - min;

        for(int i = 0; i < bucketCount; i++) {
            xList.add(" ");
            yList.add(0);
        }

        xList.set(0, Double.toString(min));
        xList.set(bucketCount - 1, Double.toString(max));

        // Finally, distribution needs to be set up:

        for (Double x : list) {
            int index = (int) Math.floor((x - min) * bucketCount / range);
            // Clamp the index:
            index = Math.min(index, bucketCount - 1);
            yList.set(index, yList.get(index) + 1);
        }

        CategoryChart chart = new CategoryChartBuilder().build();
        chart.addSeries("Distribution", xList, yList);
        chart.getStyler().setAvailableSpaceFill(1.0);
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
