package nl.utwente.simulator.entities.abstractSpecies;

import nl.utwente.simulator.simulator.AbstractSpecies;

import java.util.Arrays;

/**
 * This class is intended to be used in combination with the RadicalPositionParticle, as it contains information that the UnstructuredParticle does not
 */
public class CompositionalSpecies implements AbstractSpecies {

    public static final int I = 0;
    public static final int M = 1;
    public static final int C = 2;
    public static final int V = 3;
    public static final int EN = 4;
    public static final int EC = 5;
    public static final int MC = 6;

    /**
     * Key with:
     *  - number of half-initiators
     *  - number of non-crosslinker monomers
     *  - number of crosslinkers
     *  - number of vinyl groups
     *  - number of chain end reactive centers on non-crosslink molecules
     *  - number of chain end reactive centers on crosslinkers
     *  - number of mid-chain reactive centers on crosslinkers
     */
    public final long[] key;

    protected CompositionalSpecies(long i, long m, long c, long v, long en, long ec, long mc){
        this.key = new long[]{i,m,c,v,en,ec,mc};
    }

    @Override
    public String toString(){
        return "["+key[I]+","+key[M]+","+key[C]+","+key[V]+","+key[EN]+","+key[EC]+","+key[MC]+"]";
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(key);
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof CompositionalSpecies))                                                                      //Should never occur in simulation but doesn't hurt to check
            return false;
        CompositionalSpecies g = (CompositionalSpecies) obj;
        return (
                key[I] == g.key[I] &&
                key[M] == g.key[M] &&
                key[C] == g.key[C] &&
                key[V] == g.key[V] &&
                key[EN] == g.key[EN] &&
                key[EC] == g.key[EC] &&
                key[MC] == g.key[MC]
        );
    }
}
