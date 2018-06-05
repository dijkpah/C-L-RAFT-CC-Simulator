package nl.utwente.simulator.entities.species.structured;

public class Monomer extends Molecule{

    public final int length;

    public Monomer(int length, Molecule prev){
        super(prev);
        this.length = length;
    }
}
