package nl.utwente.simulator.simulator;

import nl.utwente.simulator.entities.RadicalPosition;

public interface AbstractSpeciesFactory<PARTICLEGROUP> {

    /**
     * Creates a group for specific species,
     * particularly useful when initializing the simulator
     */
    PARTICLEGROUP abstractSpeciesForInitiatingMolecule();
    PARTICLEGROUP abstractSpeciesForMonomer();
    PARTICLEGROUP abstractSpeciesForCrosslinker();

    /**
     * @param radical ParticleGroup in which the radical is located
     * @param pos Position of active radical in polymer
     * @param vinyl ParticleGroup in which the double bond is located
     * @return Group for the combined particle, can be used to check whether combined particle group already exists
     */
    PARTICLEGROUP abstractSpeciesAfterInterMolecularReaction(PARTICLEGROUP radical, RadicalPosition pos, PARTICLEGROUP vinyl) throws RejectedReaction;

    /**
     * @param group ParticleGroup in which intramolecular propagation occurs
     * @param pos Position of active radical in polymer
     * @return Group for the new particle, can be used to check whether combined particle group already exists
     */
    PARTICLEGROUP abstractSpeciesAfterIntraMolecularReaction(PARTICLEGROUP group, RadicalPosition pos) throws RejectedReaction;

}
