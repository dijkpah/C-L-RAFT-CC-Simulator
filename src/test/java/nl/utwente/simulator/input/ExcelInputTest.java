package nl.utwente.simulator.input;

import nl.utwente.simulator.ValidationTest;
import nl.utwente.simulator.config.Settings;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static nl.utwente.simulator.config.Settings.log;
import static nl.utwente.simulator.input.ExcelInput.*;
import static nl.utwente.simulator.input.InputSource.TEST;


@Category(ValidationTest.class)
public class ExcelInputTest extends ValidationTest {

    @AfterClass
    public static void cleanup(){
        ExcelInput.deleteExcel(TEST);
    }

    @Test
    public void testExcelGenerationAndReading() throws ExcelGenerationException, IOException, ReflectiveOperationException {

        Settings.INPUT_FILE = TEST.defaultFileName();
        try {
            log.infoln("[---------------------------- VALIDATING INPUT CLASS -------------------------------]");
            validate(InputClass.class);

            log.infoln("[------------------------ GENERATING EXCEL WITH NULL VALUES ------------------------]");
            generateExcel(InputClass.class, TEST, true);

            log.infoln("[------------------------- READING EXCEL WITH NULL VALUES --------------------------]");
            readExcel(InputClass.class, TEST, true);
            log.infoln("[----------------------- READING EXCEL WITHOUT NULL VALUES -------------------------]");
            readExcel(InputClass.class, TEST, false);

            log.infoln("[---------------------- GENERATING EXCEL WITHOUT NULL VALUES -----------------------]");
            generateExcel(InputClass.class, TEST, false);

            log.infoln("[------------------------- READING EXCEL WITH NULL VALUES --------------------------]");
            readExcel(InputClass.class, TEST, true);
            log.infoln("[----------------------- READING EXCEL WITHOUT NULL VALUES -------------------------]");
            readExcel(InputClass.class, TEST, false);
        } catch (ExcelGenerationException e) {
            log.errorln("[ERROR]" + e.getMessage());
            throw e;
        }
    }

    public class TestClass {
        public class SubClass1 extends TestClass {}
        public class SubClass2 extends TestClass {
            public class SubClass3 extends SubClass2 {}
        }
    }
    public class LongClass {
        public class SubClass1 extends LongClass {}
        public class SubClass2 extends LongClass {}
        public class SubClass3 extends LongClass {}
        public class SubClass4 extends LongClass {}
    }
    public class AnnClass {
        @InputValue(value = "This is AnnotatedSubClass1", src = {TEST})
        public class AnnSubClass1 extends AnnClass {
            @InputValue(value = "This is AnnotatedSubClass2", src = {TEST})
            public class AnnSubClass2 extends AnnClass {}
            public class AnnSubClass3 extends AnnClass {}
        }
    }
    public enum TestEnum {
        VALUE_1,
        VALUE_2,
        VALUE_3
    }
}
