package nl.utwente.simulator.output.dynamicoutput;

import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.output.FileWriter;
import nl.utwente.simulator.output.dynamicoutput.functions.*;
import nl.utwente.simulator.simulator.Species;
import nl.utwente.simulator.utils.codegeneration.ExpressionGenerator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static nl.utwente.simulator.config.Settings.CSV_DELIMITER;
import static nl.utwente.simulator.config.Settings.LINE_END;

/**
 * Class monitoring all custom output functions, responsible for performing internal I/O with rest of simulation
 *
 * Offers multiple modifiers on top of <code>Expression</code>,
 * in accordance with the following regular expression, where the syntax of <code>Expression</code>
 * is specified by its corresponding ANTLR file:
 *
 *   ('AVG(' | 'BIN(' | 'INC(' | 'SUM(' | 'WAV(' | 'EXC(' | 'ZAV('){n} <code>Expression</code> ')'{n}
 *
 * - AVG: calculate (number) average value of expressions over multiple species
 * - BIN: create a data point per weight-category (on a logarithmic scale)
 * - INC: include starting molecules in data (half-initiators, monomers and crosslinkers)
 * - EXC: exclude biggest molecule in data
 * - SUM: calculate sum of expression values over multiple species
 * - WAV: calculate weight average value of expressions over multiple species
 * - WAV: calculate z-average value of expressions over multiple species
 *
 * NOTE:
 * - A dynamic output function should contain at least an AVG, SUM, ZAV or WAV modifier,
 *   as it is not feasible to create a data point per molecule (species) per conversion
 * - The order of these modifiers does not matter
 * - non-binned output is combined into a single file
 * - Modifiers cannot be used in combination with operators and only functions as top-level functions
 *
 * The following function aliases are available:
 *
 * MW: WAV(w)
 * MN: AVG(w)
 * MZ: ZAV(w)
 * MWD: BIN(SUM(w))
 * PDI: WAV(w)/AVG(w)    (cannot be created with regular dynamic output)
 *
 * These should be defined top-level and cannot be used in combination with modifiers
 * with the exception of the INC and EXC modifiers (i.e. INC(PDI) is allowed, but BIN(PDI) is not).
 * The syntax is described by the following regular expression:
 *
 *  ('INC(' | 'EXC('){n} '('MW' | 'MZ'| 'MN' | 'MND' | 'MWD' | 'MZD' | 'PDI') ')'{n}
 *
 * This is done to prevent ambiguity, as 'MW' may refer to number of monomers times molecule weight,
 * as well as molecular weight. We will thus interpret 'MW' as the former, as the latter is disallowed.
 *
 *
 */
public class DynamicOutput {

    public enum AverageType { NUMBER, WEIGHT, Z}
    public static final String[] ALIASES = {"MW", "MN", "MZ", "MND", "MWD", "MZD", "PDI"};                              //SIZE is substituted during desugaring in ExpressionGenerator
    public static final String[] MODIFIERS = {"AVG", "BIN", "INC", "SUM", "WAV", "ZAV", "EXC"};

    protected List<NonBinnedFunc> nonBinnedFuncs = new ArrayList<>();
    protected List<BinnedFunc> binnedFuncs = new ArrayList<>();
    protected List<Double> keys = new ArrayList<>();

    public void addFunc(String f) throws ParseException {
        f = f.replaceAll("\\s", "");                                                                                    //Strip whitespace
        String name = f;

        boolean binned = false;
        boolean avg = false;
        AverageType averageType = null;
        boolean sum = false;
        boolean inc = false;
        boolean exc = false;

        while(f.length()>4 && (f.toUpperCase().startsWith("INC(") || f.toUpperCase().startsWith("EXC(")) && f.endsWith(")")){//parse outermost INC separately in case alias is used
            if(f.toUpperCase().startsWith("INC(")) {
                inc = true;
            }else if(f.toUpperCase().startsWith("EXC(")){
                exc = true;
            }else{
                throw new NotImplementedException();
            }
            f = f.substring(4, f.length() - 1);                                                                         //Strip modifier
        }

        if(Arrays.asList(ALIASES).contains(f.toUpperCase())){
            switch(f.toUpperCase()){
                case "MW" :                                                                                             //WAV(w)
                    nonBinnedFuncs.add(new AverageFunc(name, ExpressionGenerator.generate("w"), inc, exc, AverageType.WEIGHT));
                    return;
                case "MN" :                                                                                             //AVG(w)
                    nonBinnedFuncs.add(new AverageFunc(name, ExpressionGenerator.generate("w"), inc, exc, AverageType.NUMBER));
                    return;
                case "MZ" :                                                                                             //ZAV(w)
                    nonBinnedFuncs.add(new AverageFunc(name, ExpressionGenerator.generate("w"), inc, exc, AverageType.Z));
                    return;
                case "MND" :                                                                                            //BIN(SUM(1))
                    binnedFuncs.add(new BinnedSumFunc(name, ExpressionGenerator.generate("1"), inc, exc));
                    return;
                case "MWD" :                                                                                            //BIN(SUM(w))
                    binnedFuncs.add(new BinnedSumFunc(name, ExpressionGenerator.generate("w"), inc, exc));
                    return;
                case "MZD" :                                                                                            //BIN(SUM(w^2))
                    binnedFuncs.add(new BinnedSumFunc(name, ExpressionGenerator.generate("w^2"), inc, exc));
                    return;
                case "PDI" :                                                                                            //BIN(WAV(w))/AVG(w)
                    nonBinnedFuncs.add(new PDIFunc(inc, exc));
                    return;
                default:
                    throw new NotImplementedException();
            }
        }

        String prefix;
        if(f.length() > 4)
            prefix = f.substring(0,3).toUpperCase();
        else
            prefix = "";

        while (Arrays.asList(MODIFIERS).contains(prefix)){
            prefix = f.substring(0,4).toUpperCase();
            if(f.endsWith(")")){
                switch (prefix){
                    case "BIN(" :
                        binned = true;
                        break;
                    case "ZAV(" :
                        if (averageType != null) throw new ParseException("Cannot combine average modifiers",0);
                        averageType = AverageType.Z;
                        avg = true;
                        break;
                    case "WAV(" :
                        if (averageType != null) throw new ParseException("Cannot combine average modifiers",0);
                        averageType = AverageType.WEIGHT;
                        avg = true;
                        break;
                    case "AVG(" :
                        if (averageType != null) throw new ParseException("Cannot combine average modifiers",0);
                        averageType = AverageType.NUMBER;
                        avg = true;
                        break;
                    case "INC(" :
                        inc = true;
                        break;
                    case "EXC(" :
                        exc = true;
                        break;
                    case "SUM(" :
                        sum = true;
                        break;
                    default:
                        throw new ParseException("Missing parenthesis", 0);
                }
            } else {
                throw new ParseException("Missing parenthesis", 0);
            }
            f = f.substring(4, f.length() - 1);                                                                         //Strip modifier
            if(f.length() > 4)
                prefix = f.substring(0,3).toUpperCase();
            else
                prefix = "";
        }

        if (!sum && !avg) {
            throw new ParseException("Custom function should contain either SUM(), ZAV(), WAV() or AVG() modifier.", 0);
        } else if (sum && avg){
            throw new ParseException("Custom function cannot contain both SUM(), ZAV(), WAV() and AVG() modifier.", 0);
        }
        Expression e = ExpressionGenerator.generate(f);

        if(binned) {
            if(avg) {
                binnedFuncs.add(new BinnedAverageFunc(name, e, inc, exc, averageType));
            }else {
                binnedFuncs.add(new BinnedSumFunc(name, e, inc, exc));
            }
        }else {
            if(avg) {
                nonBinnedFuncs.add(new AverageFunc(name, e, inc, exc, averageType));
            }else {
                nonBinnedFuncs.add(new SumFunc(name, e, inc, exc));
            }
        }
    }

    /**
     * Adds another row of output for each output function
     */
    public void addRow(double conversion, List<? extends Species> particles, Species biggestParticle){
        keys.add(conversion);

        for(NonBinnedFunc nbf : nonBinnedFuncs){
            nbf.addRow(conversion, particles, biggestParticle);
        }

        for(BinnedFunc bf : binnedFuncs){
            bf.addRow(conversion, particles, biggestParticle);
        }
    }

    /**
     * Creates an output file for each binned output function
     * and a single file containing all the non-binned output functions
     */
    public void export() throws IOException {
        for(BinnedFunc bf : binnedFuncs) {
            bf.exportCSV();                                                                                             //For binned functions simply export one per function
        }

        if(!nonBinnedFuncs.isEmpty()) {
            String contents = "'conversion(%)'" + CSV_DELIMITER;                                                        //Add column headers
            for (double key : keys) {
                contents += String.format(Locale.ROOT, "%3.2f%s", key, CSV_DELIMITER);
            }
            contents += LINE_END;

            Double data;
            for (NonBinnedFunc nbf : nonBinnedFuncs) {
                String row = nbf.function + CSV_DELIMITER;
                for (double key : keys) {                                                                               //add data per conversion
                    data = nbf.getData(key);
                    row += data != null
                        ? nbf.getData(key) + CSV_DELIMITER
                        : CSV_DELIMITER;
                }
                contents += row + LINE_END;
            }
            FileWriter.export("OTHER", "csv", contents);
        }
    }


}


