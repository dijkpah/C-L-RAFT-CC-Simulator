package nl.utwente.simulator.output;

import org.junit.Test;

import static nl.utwente.simulator.config.Settings.log;

public class ProgressBarTest {

    @Test
    public void main() {
        long total = 235;
        log.infoln();
        log.infoln("Progress:");
        ProgressBar.logProgress(0, false);
        for (int i = 1; i <= total; i = i + 3) {
            try {
                Thread.sleep(50);
                int percent = (int) (i * 100 / total);
                ProgressBar.logProgress(percent, true);
            } catch (InterruptedException e) {}
        }
    }
}
