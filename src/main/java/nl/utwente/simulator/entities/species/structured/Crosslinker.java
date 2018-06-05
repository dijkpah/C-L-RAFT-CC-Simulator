package nl.utwente.simulator.entities.species.structured;


import lombok.NonNull;

public abstract class Crosslinker extends Molecule{


    private Crosslinker(@NonNull Molecule prev) {
        super(prev);
    }

    public static class FirstHalf extends Crosslinker {
        FirstHalf(@NonNull Molecule prev){
            super(prev);
        }
    }

    public static class SecondHalf extends Crosslinker {
        SecondHalf(@NonNull Molecule prev){
            super(prev);
        }
    }



}
