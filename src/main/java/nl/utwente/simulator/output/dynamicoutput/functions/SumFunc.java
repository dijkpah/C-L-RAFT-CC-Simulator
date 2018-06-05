package nl.utwente.simulator.output.dynamicoutput.functions;

import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.output.dynamicoutput.NonBinnedFunc;
import nl.utwente.simulator.simulator.Species;

import java.util.List;

public class SumFunc extends NonBinnedFunc {


    public SumFunc(String f, Expression e, boolean includeInitialMolecules, boolean excludeBiggestMolecule) {
        super(f,e,includeInitialMolecules, excludeBiggestMolecule);
    }

    @Override
    protected void addRow(double conversion, List<? extends Species> particles, Species biggestParticle) {

        Double datapoint = 0d;
        for(Species p : particles){
            if(super.includeInitialMolecules || (p.isPolymer())) {
                datapoint += p.number() * (long) expression.evaluate(p);
            }
        }

        if(super.excludeBiggestMolecule){
            datapoint-= (long) expression.evaluate(biggestParticle);
        }

        addData(conversion, datapoint);
    }

}
