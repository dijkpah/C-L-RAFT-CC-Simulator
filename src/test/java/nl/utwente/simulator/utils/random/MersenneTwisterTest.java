package nl.utwente.simulator.utils.random;

import org.apache.commons.math3.random.MersenneTwister;

public class MersenneTwisterTest extends RandomTest{

    MersenneTwister mt = new MersenneTwister();

    @Override
    public long getRandom(long min, long max){
        return mt.nextLong(max-min) + min;
    }

    @Override
    public long getRandom(long max) {
        return mt.nextLong(max);
    }

    @Override
    public String getName() {
        return "MersenneTwister";
    }
}
