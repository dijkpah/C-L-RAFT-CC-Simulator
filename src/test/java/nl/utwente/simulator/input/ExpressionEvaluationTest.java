package nl.utwente.simulator.input;

import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.utils.codegeneration.ExpressionGenerator;
import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.IExpressionEvaluator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExpressionEvaluationTest {

    public static long I = 6;
    public static long M = 300;
    public static long C = 100;
    public static long P = 50;
    public static long R = 6;
    public static double l = 0.01;

    @Test
    public void testCustomFunction() throws Exception {
        IExpressionEvaluator ee = CompilerFactoryFactory.getDefaultCompilerFactory().newExpressionEvaluator();
        ee.setExpressionType(double.class);
        ee.setParameters(
            new String[] {"I", "M", "C", "P", "R", "EN", "EC", "MC"},
            new Class[] { long.class, long.class, long.class, long.class, long.class, long.class, long.class, long.class }
        );
        ee.cook("Math.pow("+l+"*M, 1.5) * Math.pow(Math.sqrt(1+(C-P)/6.0) +   (C-P)/2.35619449019, -0.75) * 0.28501107337");

        String function = "("+l+"*M)^1.5 (√(1+(C-P)/6.0) + (C-P)/2.35619449019)^-0.75 * 0.28501107337";

        Expression ve = ExpressionGenerator.generate(function);

        long startTime;
        long endTime;
        int samples = 20000000;

        assertEquals(f1(), (double) ee.evaluate(new Object[] {I,M,C,P,R,0,0,0 }),0.00000001d);
        assertEquals(f1(), ve.evaluate(I,M,C,P,R,0,0,0 ),0.00000001d);

        M=300;
        startTime = System.currentTimeMillis();
        for(int j=0;j<samples;j++){
            M++;
            double result = (double) ee.evaluate(new Object[] {I,M,C,P,R,0,0,0 });
            if(result<0)
                assertTrue(false);
        }
        endTime = System.currentTimeMillis();
        System.out.println("Interpreted Janino(ms):"+(endTime-startTime));

        M=300;
        startTime = System.currentTimeMillis();
        for(int j=0;j<samples;j++){
            M++;
            double result = ve.evaluate(I,M,C,P,R,0,0,0 );
            if(result<0)
                assertTrue(false);
        }
        endTime = System.currentTimeMillis();
        System.out.println("Compiled Janino(ms):"+(endTime-startTime));

        M=300;
        startTime = System.currentTimeMillis();
        for(int j=0;j<samples;j++){
            M++;
            double result = f1();
            if(result<0)
                assertTrue(false);
        }
        endTime = System.currentTimeMillis();
        System.out.println("Native Java(ms):"+(endTime-startTime));
    }

    private static double f1(){
        return Math.pow(l*M, 1.5) * Math.pow(Math.sqrt(1+(C-P)/6.0) + (C-P)/2.35619449019, -0.75) * 0.28501107337;
    }
}
