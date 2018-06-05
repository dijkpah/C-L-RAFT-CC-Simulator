package nl.utwente.simulator.entities.abstractSpecies;

import nl.utwente.simulator.simulator.AbstractSpecies;

import java.util.Arrays;

public class SizeSpecies implements AbstractSpecies {


    public static final int SIZE = 0;
    public static final int V = 1;
    public static final int EN = 2;
    public static final int EC = 3;
    public static final int MC = 4;

    /**
     * Key with:
     *  - size: combined number of half-initiators, monomers and crosslinkers
     *  - v: number of vinyl groups
     *  - en: number of chain end reactiveCenters on non-crosslinker molecules
     *  - ec: number of chain end reactiveCenters on crosslinkers
     *  - mc: number of mid-chain reactiveCenters on crosslinkers
     */
    public final long[] key;

    protected SizeSpecies(long size, long v, long en, long ec, long mc){
        this.key = new long[]{size,v,en,ec,mc};
    }

    @Override
    public String toString(){
        return "["+key[SIZE]+","+key[V]+","+key[EN]+","+key[EC]+","+key[MC]+"]";
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(key);
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof SizeSpecies))                                                                      //Should never occur in simulation but doesn't hurt to check
            return false;
        SizeSpecies g = (SizeSpecies) obj;
        return (
                key[SIZE] == g.key[SIZE] &&
                key[V]  == g.key[V] &&
                key[EN] == g.key[EN] &&
                key[EC] == g.key[EC] &&
                key[MC] == g.key[MC]
        );
    }


}
