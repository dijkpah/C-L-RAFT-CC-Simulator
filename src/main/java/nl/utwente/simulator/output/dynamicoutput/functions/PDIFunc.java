package nl.utwente.simulator.output.dynamicoutput.functions;

import nl.utwente.simulator.output.dynamicoutput.NonBinnedFunc;
import nl.utwente.simulator.simulator.Species;

import java.util.List;

public class PDIFunc extends NonBinnedFunc{

    public PDIFunc(boolean includeInitialMolecules, boolean excludeBiggestMolecule) {
        super(
                includeInitialMolecules
                        ? excludeBiggestMolecule
                            ? "EXC(INC(PDI))"
                            : "INC(PDI)"
                        : excludeBiggestMolecule
                            ? "EXC(PDI)"
                            : "PDI"
            , null, includeInitialMolecules, excludeBiggestMolecule
        );
    }

    @Override
    protected void addRow(double conversion, List<? extends Species> particles, Species biggestParticle) {
        double number = 0;
        double numberTimesMass = 0;
        double numberTimesMassSquared = 0;

        for(Species p : particles){
            if(super.includeInitialMolecules || (p.isPolymer())) {
                double mass = p.getWeight();
                double nr = p.number();

                number += nr;
                numberTimesMass += nr * mass;
                numberTimesMassSquared += nr * mass * mass;
            }
        }

        if(super.excludeBiggestMolecule){
            double w = biggestParticle.getWeight();
            number--;
            numberTimesMass -= w;
            numberTimesMassSquared -= w*w;
        }

        if(number != 0)
            addData(conversion,(numberTimesMassSquared/numberTimesMass)/ (numberTimesMass/number));                     //Mn/Mw
    }
}
