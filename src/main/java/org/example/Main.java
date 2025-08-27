package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        Graph.getInstance().addFunction(0, 2 * Math.PI, 1000, Math::sin);
        Graph.getInstance().addFunction(0, 2 * Math.PI, 1000, Math::cos);
        Graph.getInstance().draw();
    }
}