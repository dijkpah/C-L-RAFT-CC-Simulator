package nl.utwente.simulator.output.dynamicoutput.functions;

import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.output.dynamicoutput.BinnedFunc;
import nl.utwente.simulator.output.dynamicoutput.DynamicOutput;
import nl.utwente.simulator.simulator.Species;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;


/**
 * Custom output function that calculates average <code>expression</code> for each particle
 * per weight category per conversion point.
 */
public class BinnedAverageFunc extends BinnedFunc {

    public final DynamicOutput.AverageType type;

    public BinnedAverageFunc(String f, Expression e, boolean includeInitialMolecules, boolean excludeBiggestMolecule, DynamicOutput.AverageType type) {
        super(f,e, includeInitialMolecules, excludeBiggestMolecule);
        this.type = type;
    }

    @Override
    protected void addRow(double conversion, List<? extends Species> particles, Species biggestParticle) {
        keys.add(conversion);
        double[] row       = new double[MAX_BINS];
        double[] countsRow = new double[MAX_BINS];
        Double[] data      = new Double[MAX_BINS];

        double val;
        double weight;
        long num;

        for(Species p : particles){
            if(super.includeInitialMolecules || (p.isPolymer())) {

                weight = p.getWeight();
                num = p.number();
                val = expression.evaluate(p);

                switch(type) {
                    case NUMBER:
                        row[getBinNumber((long) weight)] += num * val;
                        countsRow[getBinNumber((long) weight)] += num;
                        break;
                    case WEIGHT:
                        row[getBinNumber((long) weight)] += num * weight * val;
                        countsRow[getBinNumber((long) weight)] += num * weight;
                        break;
                    case Z:
                        row[getBinNumber((long) weight)] += num * weight * weight * val;
                        countsRow[getBinNumber((long) weight)] += num * weight * weight;
                        break;
                    default:
                        throw new NotImplementedException();
                }
            }
        }


        if(super.excludeBiggestMolecule){

            weight = biggestParticle.getWeight();
            val = expression.evaluate(biggestParticle);

            switch(type) {
                case NUMBER:
                    row[getBinNumber((long) weight)] -= val;
                    countsRow[getBinNumber((long) weight)] --;
                    break;
                case WEIGHT:
                    row[getBinNumber((long) weight)] -= weight * val;
                    countsRow[getBinNumber((long) weight)] -= weight;
                    break;
                case Z:
                    row[getBinNumber((long) weight)] -= weight * weight * val;
                    countsRow[getBinNumber((long) weight)] -= weight * weight;
                    break;
                default:
                    throw new NotImplementedException();
            }
        }

        for(int i=0;i<data.length;i++){
            if(countsRow[i] >=1)
                data[i] = row[i]/countsRow[i];
        }

        dataTable.put(conversion, data);
    }
}
