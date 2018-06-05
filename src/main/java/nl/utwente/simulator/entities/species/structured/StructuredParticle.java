package nl.utwente.simulator.entities.species.structured;

import lombok.Getter;
import nl.utwente.simulator.entities.RadicalPosition;
import nl.utwente.simulator.simulator.Species;
import nl.utwente.simulator.simulator.RejectedReaction;
import nl.utwente.simulator.utils.random.Random;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static nl.utwente.simulator.config.Settings.CROSSLINKER_VINYL_GROUPS;

/**
 * This particle class keeps track of the exact structure of the particle it represents. The structure is described
 * in terms of monomer chains and the way they are linked together.
 */
public class StructuredParticle extends Species {

    public final double localConcentration;
    protected final Molecule[] reactiveCenters;
    public final long numberOfI;
    public final long numberOfM;
    public final long numberOfC;
    public final long numberOfENRadicals;//chain end non-cross-linker reactiveCenters
    public final long numberOfECRadicals;//chain end cross-linker reactiveCenters
    public final long numberOfMCRadicals;//mid-chain (cross-linker) reactiveCenters
    protected final CrossLink.Pointer[] activeVinylGroups;

    @Getter
    protected CrossLinkTree crosslinks;

    protected StructuredParticle(long number, Type type, long numberOfI, long numberOfM, long numberOfC, long numberOfActiveVinylGroups, long numberOfENRadicals, long numberOfECRadicals, long numberOfMCRadicals) {
        super(number, type);
        this.numberOfI = numberOfI;
        this.numberOfM = numberOfM;
        this.numberOfC = numberOfC;
        this.numberOfMCRadicals = numberOfMCRadicals;
        this.numberOfENRadicals = numberOfENRadicals;
        this.numberOfECRadicals = numberOfECRadicals;
        this.crosslinks = new CrossLinkTree.EMPTYLIST();

        reactiveCenters = new Molecule[(int)(numberOfMCRadicals + numberOfENRadicals + numberOfECRadicals)];
        activeVinylGroups = new CrossLink.Pointer[(int) numberOfActiveVinylGroups];

        if(type == Type.I){
            reactiveCenters[0] = Initiating.getInstance();
        }

        this.localConcentration = calculateLocalConcentration();

        assert(localConcentration >= 0 || type != Type.POLYMER);
        assert(!(type== Type.I && ((int)(numberOfMCRadicals + numberOfENRadicals + numberOfECRadicals) != 1 || numberOfActiveVinylGroups != 0)));
        assert(!(type== Type.M && ((int)(numberOfMCRadicals + numberOfENRadicals + numberOfECRadicals) != 0 || numberOfActiveVinylGroups != 1)));
        assert(!(type== Type.C && ((int)(numberOfMCRadicals + numberOfENRadicals + numberOfECRadicals) != 0 || numberOfActiveVinylGroups != CROSSLINKER_VINYL_GROUPS)));
    }

    public long size() { return numberOfM + numberOfC + numberOfI; }
    public long numberOfI() { return numberOfI; }
    public long numberOfC() { return numberOfC; }
    public long numberOfM() { return numberOfM; }
    public long numberOfActiveVinylGroups() { return activeVinylGroups.length; }
    public long numberOfRadicals() { return reactiveCenters.length; }

    public int randomVinylGroupIndex(){ return Random.getRandom(activeVinylGroups.length); }
    public CrossLink.Pointer randomVinylGroup(){ return activeVinylGroups[randomVinylGroupIndex()]; }
    public CrossLink.Pointer randomVinylGroup(int radicalIndex) throws RejectedReaction {
        int r = randomVinylGroupIndex();
        if (reactiveCenters[radicalIndex] instanceof Crosslinker.FirstHalf && activeVinylGroups[r].chainNr == radicalIndex) {               //We should not propagate CrossLinkerSecondHalf to the cross-linked CrossLinkerFirstHalf
            throw new RejectedReaction();
        }
        return activeVinylGroups[randomVinylGroupIndex()];
    }
    
    @Override
    public RadicalPosition positionOfNthRadical(int n) {
        Molecule selectedRadical = reactiveCenters[n];
        if(selectedRadical instanceof Crosslinker.FirstHalf)
            return RadicalPosition.CHAIN_END_CROSSLINKER;
        else if(selectedRadical instanceof Crosslinker.SecondHalf)
            return RadicalPosition.MID_CHAIN_CROSSLINKER;
        else
            return RadicalPosition.CHAIN_END_NON_CROSSLINKER;
    }

    public List<Molecule> getReactiveCenters(){
        return Collections.unmodifiableList(Arrays.asList(reactiveCenters));
    }

    public List<CrossLink.Pointer> getActiveVinylGroups(){
        return Collections.unmodifiableList(Arrays.asList(activeVinylGroups));
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
