package nl.utwente.simulator.utils.random;

import java.util.concurrent.ThreadLocalRandom;

public class Random {

    public static int getRandom(int max) {
        return ThreadLocalRandom.current().nextInt(0, max);
    }

    public static long getRandom(long max) {
        return ThreadLocalRandom.current().nextLong(0, max);
    }

    public static double getRandom(double max) {
        return ThreadLocalRandom.current().nextDouble(0.0, max);
    }
}
