package nl.utwente.simulator.output.dynamicoutput.functions;

import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.output.dynamicoutput.DynamicOutput;
import nl.utwente.simulator.output.dynamicoutput.NonBinnedFunc;
import nl.utwente.simulator.simulator.Species;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class AverageFunc extends NonBinnedFunc {

    public final DynamicOutput.AverageType type;

    public AverageFunc(String f, Expression e, boolean includeInitialMolecules, boolean excludeBiggestMolecule, DynamicOutput.AverageType type) {
        super(f,e, includeInitialMolecules, excludeBiggestMolecule);
        this.type = type;
    }

    @Override
    protected void addRow(double conversion, List<? extends Species> particles, Species biggestParticle) {
        long count = 0;
        double datapoint = 0;

        long num;
        double val;
        double weight;

        for(Species p : particles){

            if(super.includeInitialMolecules || (p.isPolymer())){
                num = p.number();
                val = expression.evaluate(p);

                switch(type){
                    case NUMBER:
                        count += num;
                        datapoint += num * val;
                        break;
                    case WEIGHT:
                        weight = p.getWeight();
                        count += num * weight;
                        datapoint += num * weight * val;
                        break;
                    case Z:
                        weight = p.getWeight();
                        count += num * weight * weight;
                        datapoint += num * weight * weight * val;
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
                    count--;
                    datapoint -= val;
                    break;
                case WEIGHT:
                    count -= weight;
                    datapoint -= weight * val;
                    break;
                case Z:
                    count -= weight * weight;
                    datapoint -= weight * weight * val;
                    break;
                default:
                    throw new NotImplementedException();
            }
        }

        if(count >= 1) {                                                                                                 //only data points
            addData(conversion, datapoint / count);
        }
    }

}
