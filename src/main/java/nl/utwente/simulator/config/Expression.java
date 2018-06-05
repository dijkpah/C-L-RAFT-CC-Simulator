package nl.utwente.simulator.config;

import nl.utwente.simulator.simulator.Species;

/**
 * A mathematical expression containing the following particle properties:
 *
 * I: number of half-initiators in the particle
 * M: number of monomers in the particle
 * C: number of crosslinkers in the particle
 * P: number of pendent vinyl groups in the particle
 * R: number of reactiveCenters in the particle
 * EN: number of chain end non-crosslinker reactiveCenters in the particle
 * EC: number of chain end crosslinker reactiveCenters in the particle
 * MC: number of mid-chain crosslinker reactiveCenters in the particle
 *
 */
public abstract class Expression {
    public abstract double evaluate(long I, long M, long C, long P, long R, long EN, long EC, long MC);

    public double evaluate(Species p){
        return evaluate(
                p.numberOfI(),
                p.numberOfM(),
                p.numberOfC(),
                p.numberOfActiveVinylGroups(),
                p.numberOfRadicals(),
                p.numberOfChainEndNonCrosslinkerRadicals(),
                p.numberOfChainEndCrosslinkerRadicals(),
                p.numberOfMidChainRadicals()
        );
    }
}