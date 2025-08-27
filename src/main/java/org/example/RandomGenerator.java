package org.example;
import java.util.Random;

public class RandomGenerator {

    private static RandomGenerator singleton;
    private final Random random;

    private RandomGenerator() {
        this.random = new Random();
    }

    public static RandomGenerator getInstance() {
        if(singleton == null) {
            singleton = new RandomGenerator();
        }
        return singleton;
    }

    double nextGaussian() {
        return random.nextGaussian();
    }
}
