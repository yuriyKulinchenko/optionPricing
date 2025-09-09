package org.example;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import javafx.scene.chart.XYChart;

import java.awt.*;
import java.util.List;

public class UIGraph {

    public static final LineChart<Number, Number> simulationChart;
    public static final LineChart<Number, Number> varianceChart;

    static {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Asset price");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

        chart.setTitle("Simulation paths");
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);

        simulationChart = chart;
    }

    static {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Iterations");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Variance");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

        chart.setTitle("Variance plot");
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);

        varianceChart = chart;
    }

    public static void populateSimulationChart(List<List<Vector2D>> paths) {
        simulationChart.getData().clear();
        for (int i = 0; (i < paths.size() && i < 5); i++) {
            List<Vector2D> path = paths.get(i);
            addSimulation(path, "Asset #" + (i + 1));
        }
    }

    private static void addSimulation(List<Vector2D> line, String name) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);

        series.getData().addAll(line
                .stream()
                .map(v -> new XYChart.Data<Number, Number>(v.getX(), v.getY()))
                .toList());

        UIGraph.simulationChart.getData().add(series);
    }

    private static void addDerivative(Derivative derivative) {

        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        if(derivative instanceof AsianOption option) {
            XYChart.Data<Number, Number> v0 = new XYChart.Data<>(0, option.strikePrice);
            XYChart.Data<Number, Number> v1 = new XYChart.Data<>(option.maturity, option.strikePrice);
            series.getData().addAll(v0, v1);
        } else if(derivative instanceof EuropeanOption option) {
            XYChart.Data<Number, Number> v0 = new XYChart.Data<>(0, option.strikePrice);
            XYChart.Data<Number, Number> v1 = new XYChart.Data<>(option.maturity, option.strikePrice);
            series.getData().addAll(v0, v1);
        } else if(derivative instanceof Barrier barrier) {
            Derivative underlying = barrier.derivative;
            addDerivative(underlying);
            XYChart.Data<Number, Number> v0 = new XYChart.Data<>(0, barrier.barrier);
            XYChart.Data<Number, Number> v1 = new XYChart.Data<>(underlying.getMaturity(), barrier.barrier);
            series.getData().addAll(v0, v1);
        }

        UIGraph.simulationChart.getData().add(series);
    }

    public static void populateVarianceChart(List<Double> sums, List<Double> squares, int chunkSize) {
        varianceChart.getData().clear();
        double sum = 0;
        double square = 0;
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for(int i = 0; i < sums.size(); i++) {
            sum += sums.get(i);
            square += squares.get(i);
            double N = chunkSize * (i + 1);

            double mean = sum / N;
            double squareMean = square / N;

            double variance = squareMean - mean * mean;

            series.getData().add(new XYChart.Data<>(N, variance / N));
        }

        varianceChart.getData().add(series);
    }
}