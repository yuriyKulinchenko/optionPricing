package org.example;
import org.apache.commons.math3.random.SobolSequenceGenerator;
import org.apache.commons.math3.distribution.NormalDistribution;

public class Sobel {

    private static int index = 0;


    public static synchronized SobolSequenceGenerator getGenerator(int d, int N) {
        SobolSequenceGenerator generator = new SobolSequenceGenerator(d);
        generator.skipTo(index);
        index += N;
        return generator;
    }

    public static void transformToNormal(double[] uniform) {
        NormalDistribution distribution = new NormalDistribution();
        for(int i = 0; i < uniform.length; i++) {
            uniform[i] = distribution.inverseCumulativeProbability(uniform[i]);
        }
    }

}
