package nl.utwente.simulator.simulator;

public class RejectedReaction extends Exception{

    public RejectedReaction(){
        super("Reaction did not succeed (possibly due to steric hindrance)");
    }
}
