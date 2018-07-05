package nl.utwente.simulator.entities.species.structured;


import javax.annotation.Nonnull;

public abstract class Crosslinker extends Molecule{


    private Crosslinker(@Nonnull Molecule prev) {
        super(prev);
    }

    public static class FirstHalf extends Crosslinker {
        FirstHalf(@Nonnull Molecule prev){
            super(prev);
        }
    }

    public static class SecondHalf extends Crosslinker {
        SecondHalf(@Nonnull Molecule prev){
            super(prev);
        }
    }



}
