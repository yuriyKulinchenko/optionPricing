package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) {
        GeometricBrownianMotion stock = new GeometricBrownianMotion(1, 1, 3);
        for (int i = 0; i < 5; i++) {
            List<Double> list = stock.simulateSteps(1000, 0.1);
            Graph.getInstance().addSeries("stock " + i, list);
            stock.reset();
        }
        Graph.getInstance().draw();
    }
}