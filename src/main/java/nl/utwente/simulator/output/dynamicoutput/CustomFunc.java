package nl.utwente.simulator.output.dynamicoutput;

import nl.utwente.simulator.config.Expression;
import nl.utwente.simulator.simulator.Species;

import java.util.List;

/**
 * A custom function with string representation <code>function</code> and implementation <code>expression</code>.
 * This expression evaluates a single particle.
 *
 * For every point of conversion this expression is used to add a single row of output, given the whole set of species
 * at that point of conversion as input.
 */
public abstract class CustomFunc {

    protected final String function;
    protected final Expression expression;
    protected final boolean includeInitialMolecules;
    protected final boolean excludeBiggestMolecule;

    protected CustomFunc(String function, Expression expression, boolean includeInitialMolecules, boolean excludeBiggestMolecule){
        this.function = function;
        this.expression = expression;
        this.includeInitialMolecules = includeInitialMolecules;
        this.excludeBiggestMolecule = excludeBiggestMolecule;
    }

    protected abstract void addRow(double conversion, List<? extends Species> particles, Species biggestParticle);

    public static String convertToFileName(String f){
        return f
            .replace(" ","")
            .replace("*","×")
            .replace("/", "∕")
            .replace("π", "pi")
            .replace("√", "sqrt")
            .replace(".", ",");
    }
}
