package nl.utwente.simulator.output.dynamicoutput.functions;

import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.output.dynamicoutput.BinnedFunc;
import nl.utwente.simulator.simulator.Species;

import java.util.List;

public class BinnedSumFunc extends BinnedFunc {

    public BinnedSumFunc(String f, Expression e, boolean includeInitialMolecules, boolean excludeBiggestMolecule) {
        super(f,e, includeInitialMolecules, excludeBiggestMolecule);
    }

    @Override
    protected void addRow(double conversion, List<? extends Species> particles, Species biggestParticle) {
        keys.add(conversion);
        double[] row = new double[MAX_BINS];
        Double[] data = new Double[MAX_BINS];

        for(Species p : particles){
            if(super.includeInitialMolecules || (p.isPolymer())) {
                row[getBinNumber((long) p.getWeight())] += p.number() * expression.evaluate(p);
            }
        }

        if(super.excludeBiggestMolecule){
            row[getBinNumber((long) biggestParticle.getWeight())] -= expression.evaluate(biggestParticle);
        }

        for(int i=0;i<MAX_BINS;i++){                                                                                    //Alternatively we could check if a bin has been initialized
            data[i] = row[i];                                                                                           //and set value to zero if not
        }
        dataTable.put(conversion, data);
    }
}
