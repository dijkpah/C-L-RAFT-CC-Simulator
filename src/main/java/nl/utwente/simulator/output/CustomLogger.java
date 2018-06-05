package nl.utwente.simulator.output;

import nl.utwente.simulator.config.Settings;
import nl.utwente.simulator.utils.ColoredConsoleAppender;
import org.apache.log4j.*;

import static nl.utwente.simulator.config.Settings.*;

/**
 * Acts as a wrapper for the log4j Logger
 * adding the ability to easily switch between default and colored output
 * adding the ability to print with line endings, like System.out
 * adding default log level to ALL when assertions are enabled
 */
public class CustomLogger{

    Logger log;

    public CustomLogger(Class clazz) {
        log = LogManager.getLogger(clazz.getName());
        this.setStyle(LOG_FORMAT, LOG_COLORED);
        if(ASSERTIONS_ENABLED)
            log.setLevel(Level.ALL);                                                                                    //Just put it on ALL mode when we enable assertions
        else
            log.setLevel(Settings.LOG_LEVEL);
    }

    public void setStyle(String patternLayout, boolean colored){
        BasicConfigurator.resetConfiguration();
        ConsoleAppender config = colored
                ? new ColoredConsoleAppender(new PatternLayout(patternLayout))
                : new ConsoleAppender(new PatternLayout(patternLayout));
        BasicConfigurator.configure(config);
    }

    public void error(String msg){
        log.error(msg);
    }
    public void errorln(String msg){
        log.error(msg+LINE_END);
    }
    public void errorln(){
        log.error(LINE_END);
    }

    public void warn(String msg){
        log.warn(msg);
    }
    public void warnln(String msg){
        log.warn(msg+LINE_END);
    }
    public void warnln(){
        log.warn(LINE_END);
    }

    public void info(String msg){
        log.info(msg);
    }
    public void infoln(String msg){
        log.info(msg+LINE_END);
    }
    public void infoln(){
        log.info(LINE_END);
    }

    public void debug(String msg){
        log.debug(msg);
    }
    public void debugln(String msg){
        log.debug(msg+LINE_END);
    }
    public void debugln(){
        log.debug(LINE_END);
    }

    public void trace(String msg){
        log.trace(msg);
    }
    public void traceln(String msg){
        log.trace(msg+LINE_END);
    }
    public void traceln(){
        log.trace(LINE_END);
    }

    public void setLevel(Level lvl){
        log.setLevel(lvl);
    }
}
