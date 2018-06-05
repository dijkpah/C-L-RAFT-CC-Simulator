package nl.utwente.simulator.output;

import nl.utwente.simulator.ValidationTest;
import nl.utwente.simulator.config.Settings;
import nl.utwente.simulator.output.dynamicoutput.functions.BinnedSumFunc;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.text.ParseException;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.StrictMath.round;
import static nl.utwente.simulator.output.dynamicoutput.BinnedFunc.G;
import static nl.utwente.simulator.output.dynamicoutput.BinnedFunc.POINTS;
import static org.junit.Assert.assertEquals;

@Category(ValidationTest.class)
public class LogScaleTest extends ValidationTest {

    @Test
    public void test () throws ParseException {
        Settings.init();
        BinnedSumFunc scaleGenerator = new BinnedSumFunc("", null, false, false);
        Long startTime = System.currentTimeMillis();

        double G2 = pow(G, 1/POINTS);
        double prev = 1;
        double tmp = G2;

        int NR_OMITTED = 0;
        int NR_REPLACEMENTS = 0;
        int DIFF;

        while(round(tmp)-round(prev) <2){                                                                               //We have non-exponentional growth due to rounding problems (1.0, 2.0, 2.0, 3.0, 3.0,...)

            System.out.println( "omitted: (" +round(prev)+ "-" + round(tmp) + "] : " + round(prev));
            prev = tmp;                                                                                                 //So we calculate when this rounding problem stops (when there is a difference > 1 between
            tmp *= G2;                                                                                                  //bin values (after rounding)
            NR_OMITTED++;
        }

        int OFFSET = (int) round(prev);

        for(int i=1;i<OFFSET;i++) {                                                                                     //replacement entries
            System.out.println( "replacement: (" +i + "-" + (i+1) + "] : " + (i-1));
            assertEquals(i-1, scaleGenerator.getBinNumber(i));
            NR_REPLACEMENTS++;
        }

        DIFF = NR_OMITTED - NR_REPLACEMENTS;

        System.out.println("[------------------------------------------------------------------------]");

        for(int i=NR_OMITTED;i<50+NR_OMITTED;i++){                                                                               //We skipped OFFSET entries and added 2 manually
            long min = round(pow(G, i/POINTS));                                                                         //inclusive
            long max = round(pow(G, (i+1)/POINTS));                                                                     //exclusive

            System.out.println( "(" +min + "-" + max + "] : " + (round((POINTS* log(min)/ log(G)))-DIFF));

            for(long j=min;j<max;j++){
                assertEquals((round((POINTS* log(min)/ log(G)))-DIFF), scaleGenerator.getBinNumber(j));
            }
        }

        System.out.println("Time: "+(System.currentTimeMillis()-startTime) + " ms");


    }
}
