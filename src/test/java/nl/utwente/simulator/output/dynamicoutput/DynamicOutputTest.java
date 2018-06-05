package nl.utwente.simulator.output.dynamicoutput;

import nl.utwente.simulator.ValidationTest;
import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.config.Settings;
import nl.utwente.simulator.output.dynamicoutput.functions.*;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DynamicOutputTest extends ValidationTest {

    private DynamicOutput dynamicOutput;

    @Before
    public void init() throws ParseException {
        Settings.init();
        dynamicOutput = new DynamicOutput();
        assertTrue(dynamicOutput.binnedFuncs.isEmpty());
        assertTrue(dynamicOutput.nonBinnedFuncs.isEmpty());
    }

    @Test
    public void testModifierOrder() throws ParseException {

        dynamicOutput.addFunc("INC(BIN(AVG(1)))");
        dynamicOutput.addFunc("INC(AVG(BIN(1)))");
        dynamicOutput.addFunc("BIN(AVG(INC(1)))");
        dynamicOutput.addFunc("BIN(INC(AVG(1)))");
        dynamicOutput.addFunc("AVG(BIN(INC(1)))");
        dynamicOutput.addFunc("AVG(INC(BIN(1)))");

        assertEquals(dynamicOutput.binnedFuncs.size(), 6);
        assertTrue(dynamicOutput.nonBinnedFuncs.isEmpty());

        Expression e = dynamicOutput.binnedFuncs.get(0).expression;

        for(BinnedFunc bf : dynamicOutput.binnedFuncs){
            assertTrue(bf instanceof BinnedAverageFunc);
            assertTrue(bf.includeInitialMolecules);
        }
    }

    @Test
    public void testCapitalization() throws ParseException {

        dynamicOutput.addFunc("AVG(1)");
        dynamicOutput.addFunc("Avg(1)");
        dynamicOutput.addFunc("avg(1)");

        assertEquals(dynamicOutput.nonBinnedFuncs.size(), 3);

        for(NonBinnedFunc nbf: dynamicOutput.nonBinnedFuncs ) {
            assertTrue(nbf instanceof AverageFunc);
            assertFalse(nbf.includeInitialMolecules);
            assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.NUMBER);
        }
    }

    @Test
    public void testBinnedModifierCombinations() throws ParseException {
        int nrBinned = 0;
        BinnedFunc bf;

        //Inclusive binned (number) average

        dynamicOutput.addFunc("INC(BIN(AVG(1)))");
        nrBinned++;

        bf = lastBinnedFunc();
        assertEquals(dynamicOutput.binnedFuncs.size(), nrBinned);
        assertTrue(bf instanceof BinnedAverageFunc);
        assertTrue(bf.includeInitialMolecules);
        assertTrue(((BinnedAverageFunc) bf).type == DynamicOutput.AverageType.NUMBER);

        //Inclusive binned weight average

        dynamicOutput.addFunc("INC(BIN(WAV(1)))");
        nrBinned++;

        bf = lastBinnedFunc();
        assertEquals(dynamicOutput.binnedFuncs.size(), nrBinned);
        assertTrue(bf instanceof BinnedAverageFunc);
        assertTrue(bf.includeInitialMolecules);
        assertTrue(((BinnedAverageFunc) bf).type == DynamicOutput.AverageType.WEIGHT);

        //Inclusive binned sum

        dynamicOutput.addFunc("INC(BIN(SUM(1)))");
        nrBinned++;

        bf = lastBinnedFunc();
        assertEquals(dynamicOutput.binnedFuncs.size(), nrBinned);
        assertTrue(bf instanceof BinnedSumFunc);
        assertTrue(bf.includeInitialMolecules);

        //Exclusive binned sum

        dynamicOutput.addFunc("BIN(SUM(1))");
        nrBinned++;

        bf = lastBinnedFunc();
        assertEquals(dynamicOutput.binnedFuncs.size(), nrBinned);
        assertTrue(bf instanceof BinnedSumFunc);
        assertFalse(bf.includeInitialMolecules);

        //Exclusive binned (number) average

        dynamicOutput.addFunc("BIN(AVG(1))");
        nrBinned++;

        bf = lastBinnedFunc();
        assertEquals(dynamicOutput.binnedFuncs.size(), nrBinned);
        assertTrue(bf instanceof BinnedAverageFunc);
        assertFalse(bf.includeInitialMolecules);
        assertTrue(((BinnedAverageFunc) bf).type == DynamicOutput.AverageType.NUMBER);


        //Exclusive binned weight average

        dynamicOutput.addFunc("BIN(WAV(1))");
        nrBinned++;

        bf = lastBinnedFunc();
        assertEquals(dynamicOutput.binnedFuncs.size(), nrBinned);
        assertTrue(bf instanceof BinnedAverageFunc);
        assertFalse(bf.includeInitialMolecules);
        assertTrue(((BinnedAverageFunc) bf).type == DynamicOutput.AverageType.WEIGHT);

        assertTrue(dynamicOutput.nonBinnedFuncs.isEmpty());
    }

    @Test
    public void testNonBinnedModifierCombinations() throws ParseException {
        int nrNonBinned = 0;
        NonBinnedFunc nbf;

        //Inclusive non-binned sum

        dynamicOutput.addFunc("INC(SUM(1))");
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof SumFunc);
        assertTrue(nbf.includeInitialMolecules);

        //Inclusive non-binned (number) average

        dynamicOutput.addFunc("INC(AVG(1))");
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof AverageFunc);
        assertTrue(nbf.includeInitialMolecules);
        assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.NUMBER);

        //Inclusive non-binned weight average

        dynamicOutput.addFunc("INC(WAV(1))");
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof AverageFunc);
        assertTrue(nbf.includeInitialMolecules);
        assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.WEIGHT);

        //Exclusive non-binned sum

        dynamicOutput.addFunc("SUM(1)");
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof SumFunc);
        assertFalse(nbf.includeInitialMolecules);

        //Exclusive non-binned (number) average

        dynamicOutput.addFunc("AVG(1)");
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof AverageFunc);
        assertFalse(nbf.includeInitialMolecules);
        assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.NUMBER);

        //Exclusive non-binned (number) average

        dynamicOutput.addFunc("WAV(1)");
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof AverageFunc);
        assertFalse(nbf.includeInitialMolecules);
        assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.WEIGHT);

        assertTrue(dynamicOutput.binnedFuncs.isEmpty());
    }

    @Test
    public void testIncorrectModifierCombinations() throws ParseException {
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("WAV(AVG(1))"));                                 //Both WAV and AVG
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("BIN(1)"));                                      //No SUM or AVG
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("SUM(AVG(1))"));                                 //Both SUM and AVG
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("BIN(SUM(1)"));                                  //Missing parenthesis

        dynamicOutput.addFunc("AVG(Mw)");                                                                               //This is allowed, as M*w is a valid expression
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("AVG(MWD)"));                                    //These however, are not
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("AVG(MN)"));
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("AVG(PDI)"));

        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("BIN(MWD)"));                                    //This should hold for all modifier-alias combinations
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("WAV(MWD)"));                                    //with the exception of the INC modifier
        assertThrows(ParseException.class, () -> dynamicOutput.addFunc("SUM(MWD)"));
    }


    @Test
    public void testAliases() throws ParseException {
        int nrBinned = 0;
        int nrNonBinned = 0;
        BinnedFunc bf;
        NonBinnedFunc nbf;

        dynamicOutput.addFunc("MW");                                                                                    //WAV(W^2)
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof AverageFunc);
        assertFalse(nbf.includeInitialMolecules);
        assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.WEIGHT);

        dynamicOutput.addFunc("MN");                                                                                    //AVG(W)
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof AverageFunc);
        assertFalse(nbf.includeInitialMolecules);
        assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.NUMBER);

        dynamicOutput.addFunc("PDI");                                                                                   //MW/MN
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof PDIFunc);
        assertFalse(nbf.includeInitialMolecules);

        dynamicOutput.addFunc("MWD");                                                                                   //BIN(WAV(W^2))
        nrBinned++;

        bf = lastBinnedFunc();
        assertEquals(dynamicOutput.binnedFuncs.size(), nrBinned);
        assertTrue(bf instanceof BinnedSumFunc);
        assertFalse(bf.includeInitialMolecules);
    }

    @Test
    public void testAliasesWithModifiers() throws ParseException {
        int nrBinned = 0;
        int nrNonBinned = 0;
        BinnedFunc bf;
        NonBinnedFunc nbf;

        dynamicOutput.addFunc("INC(MW)");                                                                               //INC(WAV(W^2))
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof AverageFunc);
        assertTrue(nbf.includeInitialMolecules);
        assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.WEIGHT);

        dynamicOutput.addFunc("INC(MN)");                                                                               //INC(AVG(W))
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof AverageFunc);
        assertTrue(nbf.includeInitialMolecules);
        assertTrue(((AverageFunc) nbf).type == DynamicOutput.AverageType.NUMBER);

        dynamicOutput.addFunc("INC(PDI)");                                                                              //INC(MW/MN)
        nrNonBinned++;

        nbf = lastNonBinnedFunc();
        assertEquals(dynamicOutput.nonBinnedFuncs.size(), nrNonBinned);
        assertTrue(nbf instanceof PDIFunc);
        assertTrue(nbf.includeInitialMolecules);

        dynamicOutput.addFunc("INC(MWD)");                                                                              //INC(BIN(WAV(W^2)))
        nrBinned++;

        bf = lastBinnedFunc();
        assertEquals(dynamicOutput.binnedFuncs.size(), nrBinned);
        assertTrue(bf instanceof BinnedSumFunc);
        assertTrue(bf.includeInitialMolecules);
    }

    private BinnedFunc lastBinnedFunc(){
        return dynamicOutput.binnedFuncs.get(dynamicOutput.binnedFuncs.size()-1);
    }

    private NonBinnedFunc lastNonBinnedFunc(){
        return dynamicOutput.nonBinnedFuncs.get(dynamicOutput.nonBinnedFuncs.size()-1);
    }
}
