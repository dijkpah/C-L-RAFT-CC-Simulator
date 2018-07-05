package nl.utwente.simulator.entities.species.structured;

import javax.annotation.Nonnull;

public class CrossLink {
    public final Pointer firstHalf;
    public final Pointer secondHalf;

    public CrossLink(@Nonnull Pointer firstHalf, @Nonnull Pointer secondHalf) {
        this.firstHalf = firstHalf;
        this.secondHalf = secondHalf;
    }

    /**
     * This is a pointer to the one part of a cross-linker within a particle.
     * The chain number is used to decide in which chain we should look for the CrossLinkerHalf,
     * as multiple chains can contain a reference to the same CrossLinkerHalf object
     */
    public static class Pointer {
        public final Crosslinker molecule;
        public final int chainNr;

        public Pointer(Pointer p, int offset){
            this.molecule = p.molecule;
            this.chainNr = p.chainNr +offset;
        }

        public Pointer(Crosslinker molecule, int chainNr) {
            this.molecule = molecule;
            this.chainNr = chainNr;
        }
    }
}
