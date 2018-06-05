package nl.utwente.simulator.utils.random;

public class MathRandomTest extends RandomTest {

    @Override
    public long getRandom(long min, long max) {
        long range = (max - min);
        return (long)(Math.random() * range) + min;
    }

    @Override
    public long getRandom(long max) {
        return (long) (Math.random() * max);
    }


    @Override
    public String getName() {
        return "Random";
    }
}
