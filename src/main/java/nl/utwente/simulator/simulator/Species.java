package nl.utwente.simulator.simulator;

import lombok.Getter;
import lombok.Setter;
import nl.utwente.simulator.entities.RadicalPosition;

import static nl.utwente.simulator.config.Settings.*;

public abstract class Species {

    public Species(long number, Type type){this.number = number; this.type = type;}

    @Getter @Setter
    private AbstractSpecies abstractSpecies;

    private long number;
    public final Type type;

    public long number(){return number;}
    public void increaseNumber(){number++;}
    public void decreaseNumber(){number--;}

    public abstract long size();

    public abstract long numberOfI();
    public abstract long numberOfC();
    public abstract long numberOfM();

    public abstract long numberOfActiveVinylGroups();
    public abstract long numberOfChainEndCrosslinkerRadicals();
    public abstract long numberOfChainEndNonCrosslinkerRadicals();
    public abstract long numberOfMidChainRadicals();
    public abstract long numberOfRadicals();
    public abstract RadicalPosition positionOfNthRadical(int n);

    public abstract double localConcentration();

    public enum Type {POLYMER, I, M, C }

    public boolean isPolymer(){ return type== Type.POLYMER; }
    public boolean isI(){ return type== Type.I; }
    public boolean isM(){ return type== Type.M; }
    public boolean isC(){ return type== Type.C; }

    public double getWeight(){
        return
            numberOfI() * WEIGHT_HALF_INITIATOR +
            numberOfC() * WEIGHT_CROSSLINKER +
            numberOfM() * WEIGHT_MONOMER;
    }

    /**
     * Returns the local concentration of vinyl groups per nm3
     */
    protected double calculateLocalConcentration(){
        assert(reactiveVolume() > 0);
        return numberOfActiveVinylGroups() / reactiveVolume();
    }

    protected double reactiveVolume(){
        if(isPolymer())
            return Math.max(                                                                                            //Species can not be smaller than single unit
                MIN_MOL_VOLUME,
                Math.min(                                                                                               //Species can not be larger than vessel
                    COMPENSATION_FACTOR*4.1887902*Math.pow(RADIUS_EXPRESSION.evaluate(this),3),
                    VESSEL_VOLUME
                )
        );
        else{                                                                                                           //A single segment only has half the length of the repeating backbone
            return MIN_MOL_VOLUME;
        }
    }
}
