package org.example;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import javafx.scene.chart.XYChart;

import java.util.List;

public class UIGraph {

    public static Node getSimulationPaths(List<List<Vector2D>> paths) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Asset price");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Simulation paths");
        chart.setCreateSymbols(false);

        for (int i = 0; i < paths.size(); i++) {
            List<Vector2D> path = paths.get(i);
            addLine(chart, path, "Asset #" + i);
        }

        return chart;
    }

    private static void addLine(LineChart<Number, Number> chart, List<Vector2D> line, String name) {

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);

        series.getData().addAll(line
                .stream()
                .map(v -> new XYChart.Data<Number, Number>(v.getX(), v.getY()))
                .toList());

        chart.getData().add(series);
    }
}