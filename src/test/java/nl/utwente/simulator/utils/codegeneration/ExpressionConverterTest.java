package nl.utwente.simulator.utils.codegeneration;

import nl.utwente.simulator.ValidationTest;
import nl.utwente.simulator.config.Expression;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ExpressionConverterTest extends ValidationTest {

    @Test
    public void testPower() throws Exception {
        Expression v1 = ExpressionGenerator.generate("I^M^C");
        Expression v2 = ExpressionGenerator.generate("I^(M^C)");
        assertEquals(v1.evaluate(2,3,2,0,0,0,0,0), v2.evaluate(2,3,2,0,0,0,0,0),0.000001d);
        assertEquals(512, v1.evaluate(2,3,2,0,0,0,0,0),0.000001d);
    }

    @Test
    public void testDivide() throws Exception {
        Expression v1 = ExpressionGenerator.generate("1/4");
        Expression v2 = ExpressionGenerator.generate("I/M");
        Expression v3 = ExpressionGenerator.generate("I/4M");
        Expression v4 = ExpressionGenerator.generate("4I/4M");
        assertEquals(0.25, v1.evaluate(0,0,0,0,0,0,0,0),0.000001d);
        assertEquals(0.25, v2.evaluate(1,4,0,0,0,0,0,0),0.000001d);
        assertEquals(0.5,  v3.evaluate(1,2,0,0,0,0,0,0),0.000001d);
        assertEquals(2,    v4.evaluate(1,2,0,0,0,0,0,0),0.000001d);
    }

    @Test
    public void testCombination() throws Exception {
        Expression v1 = ExpressionGenerator.generate("2pi+2^3^2+4/5-6*7+8/sqrt(9)");
        assertEquals((7102d/15d)+2*Math.PI, v1.evaluate(0,0,0,0,0,0,0,0),0.000001d);
    }

    @Test
    public void testMinus() throws Exception {
        Expression v1 = ExpressionGenerator.generate("I-(-M)");
        assertEquals(5, v1.evaluate(1,4,0,0,0,0,0,0),0.000001d);
    }

    @Test
    public void testSqrt() throws Exception {
        Expression v1 = ExpressionGenerator.generate("sqrt(4)2");
        Expression v2 = ExpressionGenerator.generate("2sqrt4");
        Expression v3 = ExpressionGenerator.generate("sqrt42");
        Expression v4 = ExpressionGenerator.generate("√4I");
        assertEquals(4, v1.evaluate(0,0,0,0,0,0,0,0),0.000001d);
        assertEquals(4, v2.evaluate(0,0,0,0,0,0,0,0),0.000001d);
        assertEquals(Math.sqrt(42), v3.evaluate(0,0,0,0,0,0,0,0),0.000001d);
        assertEquals(Math.sqrt(4) * 4, v4.evaluate(4,0,0,0,0,0,0,0),0.000001d);
    }

    @Test
    public void testMC() throws Exception {
        Expression v1 = ExpressionGenerator.generate("MC");
        Expression v2 = ExpressionGenerator.generate("M*C");
        assertNotEquals(v1.evaluate(0,2,3,0,0,0,0,8), v2.evaluate(0,2,3,0,0,0,0,8),0.000001d);
    }

    @Test
    public void testPi() throws Exception {
        Expression v1 = ExpressionGenerator.generate("pi");
        Expression v2 = ExpressionGenerator.generate("π");
        assertEquals(Math.PI, v1.evaluate(0,0,0,0,0,0,0,0),0.000001d);
        assertEquals(Math.PI, v2.evaluate(0,0,0,0,0,0,0,0),0.000001d);
    }

    @Test
    public void testVolumeFormula() throws Exception {
        Expression v1 = ExpressionGenerator.generate("4/3πR^3");
        assertEquals((4.0/3.0)*Math.PI*Math.pow(2,3), v1.evaluate(0,0,0,0,2,0,0,0),0.000001d);
    }

    @Test(expected = RuntimeException.class)
    public void testUTF16Pi() throws Exception {
        ExpressionGenerator.generate("𝜋");
    }

    @Test(expected = ParseException.class)
    public void missingOpeningParenthesis() throws Exception {
        ExpressionGenerator.generate("(1*2))");
    }

    @Test(expected = ParseException.class)
    public void missingClosingParenthesis() throws Exception {
        ExpressionGenerator.generate("(1*2))");
    }

    @Test(expected = ParseException.class)
    public void mismatchedParenthesis() throws Exception {
        ExpressionGenerator.generate(")1(");
    }

    @Test(expected = ParseException.class)
    public void testIncorrect() throws Exception {
        ExpressionGenerator.generate("this is no expression");
    }
}
