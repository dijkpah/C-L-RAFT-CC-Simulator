package nl.utwente.simulator;

import nl.utwente.simulator.config.Settings;
import nl.utwente.simulator.input.InputSource;

import static nl.utwente.simulator.config.Settings.log;
import static nl.utwente.simulator.input.ExcelInput.generateExcel;
import static nl.utwente.simulator.input.ExcelInput.validate;

public class ExcelInputGenerator {

    public static void main(String[] args){
        try {
            log.infoln("Validating Settings.class annotations");
            validate(Settings.class);
            log.infoln("Generating Excel file for input source "+ InputSource.STRUCTURED);
            generateExcel(Settings.class, InputSource.STRUCTURED, false);
            log.infoln("Generating Excel file for input source "+InputSource.UNSTRUCTURED);
            generateExcel(Settings.class, InputSource.UNSTRUCTURED, false);
        } catch (Exception e) {
            log.errorln("[ERROR]"+e.getMessage());
        }
    }
}
