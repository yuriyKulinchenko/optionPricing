package org.example;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import javafx.scene.chart.XYChart;

import java.util.List;

public class UIGraph {

    public static Node getSimulationPaths(int path, List<List<Vector2D>> paths) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Asset price");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Simulation paths");

        return chart;
    }

    private static void addLine(LineChart<Double, Double> chart, List<Vector2D> line) {

        XYChart.Series<Double, Double> series = new XYChart.Series<>();

            series.getData().addAll(line
                    .stream()
                    .map(v -> new XYChart.Data<>(v.getX(), v.getY()))
                    .toList());

        chart.getData().add(series);

    }
}