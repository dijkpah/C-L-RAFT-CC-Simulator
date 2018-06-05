package nl.utwente.simulator.entities.species.unstructured;

import nl.utwente.simulator.entities.RadicalPosition;
import nl.utwente.simulator.simulator.Species;

import static nl.utwente.simulator.config.Settings.CROSSLINKER_VINYL_GROUPS;

/**
 * This particle class keeps track of the locations of reactiveCenters within the particle it represents.
 * It differentiates between Mid-chain reactiveCenters(MC), chain end reactiveCenters from monomers(M) and chain end reactiveCenters from cross-linkers(C)
 */
public class UnstructuredParticle extends Species {

    public final double localConcentration;
    public final long numberOfI;
    public final long numberOfM;
    public final long numberOfC;
    public final long numberOfActiveVinylGroups;
    public final long numberOfENRadicals;//chain end non-cross-linker reactiveCenters
    public final long numberOfECRadicals;//chain end cross-linker reactiveCenters
    public final long numberOfMCRadicals;//mid-chain (cross-linker) reactiveCenters
    public final long totalNumberOfRadicals;//total number of reactiveCenters

    //                                                     I               A               C               P                               X                        Y                        M
    protected UnstructuredParticle(long number, Type type, long numberOfI, long numberOfM, long numberOfC, long numberOfActiveVinylGroups, long numberOfENRadicals, long numberOfECRadicals, long numberOfMCRadicals) {
        super(number, type);

        this.totalNumberOfRadicals = numberOfMCRadicals + numberOfENRadicals + numberOfECRadicals;
        this.numberOfMCRadicals = numberOfMCRadicals;
        this.numberOfENRadicals = numberOfENRadicals;
        this.numberOfECRadicals = numberOfECRadicals;
        this.numberOfActiveVinylGroups = numberOfActiveVinylGroups;
        this.numberOfM = numberOfM;
        this.numberOfC = numberOfC;
        this.numberOfI = numberOfI;
        this.localConcentration = calculateLocalConcentration();
        assert(this.localConcentration >= 0 );

        assert(!(type== Type.I && (numberOfRadicals() != 1 || numberOfActiveVinylGroups != 0)));
        assert(!(type== Type.M && (numberOfRadicals() != 0 || numberOfActiveVinylGroups != 1)));
        assert(!(type== Type.C && (numberOfRadicals() != 0 || numberOfActiveVinylGroups != CROSSLINKER_VINYL_GROUPS)));
    }

    @Override
    public long size() {
        return numberOfI+numberOfM+numberOfC;
    }

    @Override
    public long numberOfI() {
        return numberOfI;
    }

    @Override
    public long numberOfC() {
        return numberOfC;
    }

    @Override
    public long numberOfM() {
        return numberOfM;
    }

    @Override
    public long numberOfActiveVinylGroups() {
        return numberOfActiveVinylGroups;
    }

    @Override
    public long numberOfRadicals() {
        return totalNumberOfRadicals;
    }

    @Override
    public RadicalPosition positionOfNthRadical(int n) {
        if(n<numberOfMCRadicals)
            return RadicalPosition.MID_CHAIN_CROSSLINKER;
        if(n<numberOfMCRadicals+numberOfECRadicals)
            return RadicalPosition.CHAIN_END_CROSSLINKER;
        else
            return RadicalPosition.CHAIN_END_NON_CROSSLINKER;
    }

    @Override
    public double localConcentration() {
        return localConcentration;
    }

    @Override
    public long numberOfChainEndCrosslinkerRadicals(){return numberOfECRadicals;}

    @Override
    public long numberOfChainEndNonCrosslinkerRadicals(){return numberOfENRadicals;}

    @Override
    public long numberOfMidChainRadicals(){return numberOfMCRadicals;}
}
