package nl.utwente.simulator.output;

import org.apache.log4j.Level;
import org.junit.Test;

public class LoggerTest {

    private static final CustomLogger log =  new CustomLogger(LoggerTest.class);

    @Test
    public void log() {
        log.setStyle("%d{yyyy-MM-dd HH:mm:ss} [%-5p] (%c{1}.java, line %L) - %m%n",false);

        log.setLevel(Level.ERROR);
        System.out.println("ERROR:");

        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warning");
        log.error("error");

        log.setLevel(Level.WARN);
        System.out.println("WARNINGS:");

        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warning");
        log.error("error");

        log.setLevel(Level.INFO);
        System.out.println("INFO:");

        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warning");
        log.error("error");

        log.setLevel(Level.DEBUG);
        System.out.println("DEBUG:");

        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warning");
        log.error("error");

        log.setLevel(Level.TRACE);
        System.out.println("TRACE:");

        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warning");
        log.error("error");
    }

    @Test
    public void coloredLog() {
        log.setStyle("%d{yyyy-MM-dd HH:mm:ss} [%-5p] (%c{1}.java, line %L) - %m%n",true);

        log.setLevel(Level.TRACE);
        System.out.println("COLORED:");

        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warning");
        log.error("error");

    }
}
