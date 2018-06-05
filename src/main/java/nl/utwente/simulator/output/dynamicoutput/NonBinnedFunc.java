package nl.utwente.simulator.output.dynamicoutput;

import nl.utwente.simulator.config.Expression;

import java.util.Map;
import java.util.TreeMap;

/**
 * This is a custom function that has only a single data point for all species per conversion point
 */
public abstract class NonBinnedFunc extends CustomFunc{

    private final Map<Double, Double> dataTable;

    protected NonBinnedFunc(String f, Expression e,boolean includeInitialMolecules, boolean excludeBiggestMolecule) {
        super(f,e,includeInitialMolecules, excludeBiggestMolecule);
        dataTable = new TreeMap<>();
    }

    protected void addData(double conversion, double data){
        dataTable.put(conversion, data);
    }

    protected Double getData(double conversion){
        return dataTable.get(conversion);
    }
}
