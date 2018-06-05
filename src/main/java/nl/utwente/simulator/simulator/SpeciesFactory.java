package nl.utwente.simulator.simulator;

public interface SpeciesFactory<P extends Species> {

    P createI(long number);

    P createM(long number);

    P createC(long number);

    /**
     * Creates a new particle which links <code>radical</code> at position <code>radicalIndexInParticle</code> and <code>vinyl</code>
     */
    P createSpecies(P radical, int radicalIndexInParticle, P vinyl) throws RejectedReaction;

    /**
     * Creates a new particle which links <code>particle</code> at position <code>radicalIndexInParticle</code> to itself
     */
    P createSpecies(P particle, int radicalIndexInParticle) throws RejectedReaction;
}
