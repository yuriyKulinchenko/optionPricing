package org.example;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.random.SobolSequenceGenerator;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Sobol {

    private static Map<Integer, Integer> indexMap = new HashMap<>();
    private static Map<Integer, double[]> deltaMap = new HashMap<>();


    private final int N;
    private int index = 0;

    private final double[] delta;
    private final SobolSequenceGenerator generator;

    public Sobol(int d, int N) {
        this.N = N;

        SobolSequenceGenerator generator = new SobolSequenceGenerator(d);
        generator.skipTo(getOffset(d, N));
        this.generator = generator;
        this.delta = getDelta(d);
    }

    private static synchronized double[] getDelta(int d) {
        if(deltaMap.containsKey(d)) return deltaMap.get(d);
        double[] delta = new double[d];
        Random random = new Random();
        for (int i = 0; i < delta.length; i++) {
            delta[i] = random.nextDouble();
        }
        return delta;
    }

    private static synchronized int getOffset(int d, int N) {
        int index = indexMap.getOrDefault(d, 0);
        indexMap.put(d, index + N);
        return index;
    }

    public double[] nextNormalVector() throws OutOfRangeException {
        index++;
        if(index > N) throw new OutOfRangeException(index, 0, N);
        double[] uniforms = generator.nextVector();
        for(int i = 0; i < uniforms.length; i++) {
            double x = uniforms[i] + delta[i];
            uniforms[i] = x - Math.floor(x);
        }
        transformToNormal(uniforms);
        return uniforms;
    }

    private static void transformToNormal(double[] uniform) {
        NormalDistribution distribution = new NormalDistribution();
        for(int i = 0; i < uniform.length; i++) {
            uniform[i] = distribution.inverseCumulativeProbability(uniform[i]);
        }
    }

}
