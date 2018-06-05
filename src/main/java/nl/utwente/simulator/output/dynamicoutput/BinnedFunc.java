package nl.utwente.simulator.output.dynamicoutput;

import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.output.FileWriter;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.*;
import static java.lang.Math.log;
import static java.lang.StrictMath.round;
import static nl.utwente.simulator.config.Settings.*;

/**
 * Custom function that calculates <code>expression</code> for each particle
 * per weight category per conversion point.
 */
public abstract class BinnedFunc extends CustomFunc{

    public static final double G = 10;
    public static final double POINTS = 10;                                                                             //Amount of points between G^i and G^(i+1)
    public static final int MAX_BINS = (int) ceil(log(
            NUMBER_HALF_INITIATORS * WEIGHT_HALF_INITIATOR +
            NUMBER_MONOMERS           * WEIGHT_MONOMER +
            NUMBER_CROSSLINKERS       * WEIGHT_CROSSLINKER
        )/log(pow(G, 1/POINTS)));

    protected final List<Double> keys;                                                                                  //Keep these separate for ordering
    protected final Map<Double, Double[]> dataTable;
    protected final long[] cachedBinMinimums;                                                                           //Makes lookups faster than calculating bin number each time

    protected BinnedFunc(String f, Expression e, boolean includeInitialMolecules, boolean excludeBiggestMolecule) {
        super(f,e, includeInitialMolecules, excludeBiggestMolecule);

        keys = new LinkedList<>();
        dataTable = new TreeMap<>();
        cachedBinMinimums = new long[MAX_BINS];

        double G2 = pow(G, 1/POINTS);
        double prev = 1;
        double tmp = G2;

        while(round(tmp)-round(prev) <2){                                                                               //We have non-exponentional growth due to rounding problems (1.0, 2.0, 2.0, 3.0, 3.0,...)
            prev = tmp;                                                                                                 //So we calculate when this rounding problem stops (when there is a difference > 1 between
            tmp *= G2;                                                                                                  //bin values (after rounding)
        }

        int max = (int) round(prev);                                                                                    //We skip those first bins and add them manually with linear growth of 1 per bin
        for(int i =1;i<=max;i++){
            cachedBinMinimums[i-1] = i;
        }

        for(int i=max;i<MAX_BINS;i++){                                                                                  //We then continue as expected by increasing the exponent for each bin value
            cachedBinMinimums[i] = round(tmp);
            tmp *= G2;
        }
    }

    public int getBinNumber(long i){                                                                                    //Perform a binary search to get the right bin numbers
        int low = 0;
        int high = cachedBinMinimums.length-1;
        int index = (high+low)/2;
        while(index!=high && index!=low){

            if(i<cachedBinMinimums[index]){
                high = index;
            }else{
                low = index;
            }
            index = (high+low)/2;//middle
        }
        return index;
    }

    protected void exportCSV() throws IOException {
        String contents = "'min weight(Da)\\conversion(%)'"+ CSV_DELIMITER;                                             //Add column headers
        for(double key : keys){
            contents += String.format("%3.2f%s", key, CSV_DELIMITER);
        }
        contents += LINE_END;

        for(int i=0;i<MAX_BINS;i++){                                                                                    //Per bin
            String row = cachedBinMinimums[i] + CSV_DELIMITER;                                                          //add row headers
            for(double key : keys) {                                                                                    //add data for each conversion point
                Double datapoint = dataTable.get(key)[i];

                row += datapoint != null
                    ? datapoint + CSV_DELIMITER
                    : CSV_DELIMITER;
            }
            contents += row+LINE_END;
        }

        FileWriter.export(
            function.equals("MWD") || function.equals("MZD") || function.equals("MND") ? function : "CUSTOM "+convertToFileName(function),
            "csv",
            contents
        );
    }
}
