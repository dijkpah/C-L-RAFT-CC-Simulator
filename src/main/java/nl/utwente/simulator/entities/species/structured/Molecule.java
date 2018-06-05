package nl.utwente.simulator.entities.species.structured;

public abstract class Molecule {

    public final Molecule prev;

    Molecule(Molecule prev){this.prev = prev;}

    @Override public String toString(){
        return "(" + ((this instanceof Initiating)
                ? "I"
                : ((this instanceof Crosslinker.FirstHalf)
                    ? "C1"
                    : ((this instanceof Crosslinker.SecondHalf)
                        ? "C2"
                        : ((this instanceof Monomer)
                            ? "M"+((Monomer) this).length
                            : "?"))))
                + ")"+ Integer.toHexString(this.hashCode()).substring(0,3);
    }

    boolean contains(Molecule m){
        if(this == m){
            return true;
        }
        if(prev ==null){
            return false;
        }
        return prev.contains(m);
    }
}
