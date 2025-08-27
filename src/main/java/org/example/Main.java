package org.example;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        double mu = 0.05;
        double sigma = 0.15;
        GeometricBrownianMotion stock = new GeometricBrownianMotion(1, mu, sigma);
        double dt = 0.01; // 0.01 years



        for (int i = 0; i < 5; i++) {
            List<Double> list = stock.simulateSteps(1000, dt); // 10 years
            Graph.getInstance().addSeries("stock " + i, list, 0, dt);
            stock.reset();
        }
        Graph.getInstance().draw();
    }
}