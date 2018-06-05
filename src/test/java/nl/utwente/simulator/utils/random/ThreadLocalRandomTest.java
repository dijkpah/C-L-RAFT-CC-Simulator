package nl.utwente.simulator.utils.random;

import java.util.concurrent.ThreadLocalRandom;

public class ThreadLocalRandomTest extends RandomTest{

    @Override
    public long getRandom(long min, long max){
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    @Override
    public long getRandom(long max) {
        return ThreadLocalRandom.current().nextLong(0L, max);
    }

    @Override
    public String getName() {
        return "ThreadLocalRandom";
    }
}
