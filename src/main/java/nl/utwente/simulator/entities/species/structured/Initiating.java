package nl.utwente.simulator.entities.species.structured;

public class Initiating extends Molecule{

    static Initiating instance;

    private Initiating(){super(null);}

    static Initiating getInstance(){
        if(instance == null) instance = new Initiating();
        return instance;
    }

}
