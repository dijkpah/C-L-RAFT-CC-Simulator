package nl.utwente.simulator.entities.species.structured;

import nl.utwente.simulator.entities.RadicalPosition;
import nl.utwente.simulator.input.InputValue;
import nl.utwente.simulator.simulator.Species;
import nl.utwente.simulator.simulator.SpeciesFactory;
import nl.utwente.simulator.simulator.RejectedReaction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static nl.utwente.simulator.config.Settings.ASSERTIONS_ENABLED;
import static nl.utwente.simulator.config.Settings.CROSSLINKER_VINYL_GROUPS;
import static nl.utwente.simulator.input.InputSource.STRUCTURED;

@InputValue(value = "A structured particle representation", src = {STRUCTURED})
public class StructuredSpeciesFactory implements SpeciesFactory<StructuredParticle> {

    @Override
    public StructuredParticle createI(long number) { return new StructuredParticle(number, Species.Type.I, 1, 0, 0, 0, 1, 0, 0); }

    @Override
    public StructuredParticle createM(long number) { return new StructuredParticle(number, Species.Type.M, 0, 1, 0, 1, 0, 0, 0); }

    @Override
    public StructuredParticle createC(long number) { return new StructuredParticle(number, Species.Type.C, 0, 0, 1, CROSSLINKER_VINYL_GROUPS, 0, 0, 0); }

    @Override
    public StructuredParticle createSpecies(StructuredParticle radical, int radIndexInParticle, StructuredParticle vinyl) {
        assert(vinyl.type   != Species.Type.I);
        assert(radical.type != Species.Type.M);
        assert(radical.type != Species.Type.C);

        byte diffEC = 0;
        byte diffEN = 0;
        byte diffMC = 0;

        RadicalPosition pos = radical.positionOfNthRadical(radIndexInParticle);

        switch(pos){
            case MID_CHAIN_CROSSLINKER:
                diffMC--;
                break;
            case CHAIN_END_NON_CROSSLINKER:
                diffEN--;
                break;
            case CHAIN_END_CROSSLINKER:
                diffEC--;
                break;
        }

        if(vinyl.isM()){
            diffEN++;
        }else if(vinyl.isC()){
            diffEC++;
        }else{
            assert(vinyl.isPolymer());
            diffMC++;
        }

        assert(diffEC+diffMC+diffEN == 0);                                                                              //Radicals do not disappear

        StructuredParticle result = new StructuredParticle(1, Species.Type.POLYMER,
                radical.numberOfI + vinyl.numberOfI,
                radical.numberOfM + vinyl.numberOfM,
                radical.numberOfC + vinyl.numberOfC,
                radical.activeVinylGroups.length + vinyl.activeVinylGroups.length - 1,
                radical.numberOfENRadicals + vinyl.numberOfENRadicals + diffEN,
                radical.numberOfECRadicals + vinyl.numberOfECRadicals + diffEC,
                radical.numberOfMCRadicals + vinyl.numberOfMCRadicals + diffMC
        );

        Molecule radicalSite = radical.reactiveCenters[radIndexInParticle];
        Molecule vinylSite;
        int vinylIndexInParticle = 0;
                                                                                                                        //First we need to create the new vinyl group within the molecule
        if(vinyl.type == Species.Type.M){                                                                              //else create new M with length 1
            vinylSite = new Monomer(1, radicalSite);
            result.crosslinks = radical.crosslinks;
        }else if(vinyl.type == Species.Type.C){                                                                        //create new CL1stHalf
            vinylSite = new Crosslinker.FirstHalf(radicalSite);                                                         //When we react with a new Crosslinker we haven't initiated the second half
            result.activeVinylGroups[vinylIndexInParticle] = new CrossLink.Pointer( (Crosslinker.FirstHalf) vinylSite, radIndexInParticle);      //so we refer to its position by pointing to the first half
            vinylIndexInParticle++;
            result.crosslinks = radical.crosslinks;
        }else{
            assert(vinyl.activeVinylGroups.length > 0);                                                                 //create new CL2ndHalf and refer to vinyl group as molecule
            CrossLink.Pointer crosslinker = vinyl.randomVinylGroup();
            vinylSite = new Crosslinker.SecondHalf(radicalSite);
            for (CrossLink.Pointer vg: vinyl.activeVinylGroups) {
                if (vg!=crosslinker) {
                    result.activeVinylGroups[vinylIndexInParticle] = new CrossLink.Pointer(vg, radical.reactiveCenters.length);
                    vinylIndexInParticle++;
                }
            }
            CrossLinkTree temp = new CrossLinkTree(radical.crosslinks, new CrossLink(                                   //We first extend the list of cross-links of the radical
                    new CrossLink.Pointer(crosslinker, radical.reactiveCenters.length),
                    new CrossLink.Pointer((Crosslinker.SecondHalf) vinylSite, radIndexInParticle)
            ));
            result.crosslinks = new CrossLinkTree(temp, vinyl.crosslinks, radical.reactiveCenters.length);                     //We then combine the two lists
        }
                                                                                                                        //Now we create the list of vinylGroups and rads for the new molecule

        for(int i = 0; i<radical.reactiveCenters.length; i++){
            result.reactiveCenters[i] = radical.reactiveCenters[i];
        }
        for(int i = 0; i<vinyl.reactiveCenters.length; i++){
            result.reactiveCenters[i+radical.reactiveCenters.length] = vinyl.reactiveCenters[i];
        }
        result.reactiveCenters[radIndexInParticle] = vinylSite;

        for(CrossLink.Pointer p : radical.activeVinylGroups){                                                           //Now we create the list of vinylGroups. We have already added some vinylGroups
            result.activeVinylGroups[vinylIndexInParticle] = p;                                                         //if the vinyl group was located on a Monomer or Species
            vinylIndexInParticle++;
        }

        assert(vinylIndexInParticle == result.activeVinylGroups.length);
        if(ASSERTIONS_ENABLED) {                                                                                        //Do not enter for-loop unless assertions are enabled
            for(CrossLink c : result.crosslinks){
                assert(result.reactiveCenters[c.firstHalf.chainNr].contains(c.firstHalf.molecule));
                assert(result.reactiveCenters[c.secondHalf.chainNr].contains(c.secondHalf.molecule));
            }
        }
        return result;
    }

    @Override
    public StructuredParticle createSpecies(StructuredParticle particle, int radIndexInParticle) throws RejectedReaction {

        RadicalPosition pos = particle.positionOfNthRadical(radIndexInParticle);
        StructuredParticle result;
        switch(pos) {
            case MID_CHAIN_CROSSLINKER:
                result = new StructuredParticle(1, Species.Type.POLYMER,
                        particle.numberOfI,
                        particle.numberOfM,
                        particle.numberOfC,
                        particle.activeVinylGroups.length - 1,
                        particle.numberOfENRadicals,
                        particle.numberOfECRadicals,
                        particle.numberOfMCRadicals
                );
                break;
            case CHAIN_END_CROSSLINKER:
                result = new StructuredParticle(1, Species.Type.POLYMER,
                        particle.numberOfI,
                        particle.numberOfM,
                        particle.numberOfC,
                        particle.activeVinylGroups.length - 1,
                        particle.numberOfENRadicals,
                        particle.numberOfECRadicals - 1,
                        particle.numberOfMCRadicals + 1
                );
                break;
            case CHAIN_END_NON_CROSSLINKER:
                result = new StructuredParticle(1, Species.Type.POLYMER,
                        particle.numberOfI,
                        particle.numberOfM,
                        particle.numberOfC,
                        particle.activeVinylGroups.length - 1,
                        particle.numberOfENRadicals - 1,
                        particle.numberOfECRadicals,
                        particle.numberOfMCRadicals + 1
                );
                break;
            default:
                throw new NotImplementedException();
        }

        Molecule radicalSite = particle.reactiveCenters[radIndexInParticle];

        CrossLink.Pointer vinyl = particle.randomVinylGroup(radIndexInParticle);                                        //create new CL2ndHalf and refer to vinyl group as molecule
        Crosslinker.SecondHalf crosslinkSite = new Crosslinker.SecondHalf(radicalSite);

        for (int i = 0; i < particle.reactiveCenters.length; i++) {
            result.reactiveCenters[i] = particle.reactiveCenters[i];
        }
        result.reactiveCenters[radIndexInParticle] = crosslinkSite;

        int vinylIndex = 0;
        for (CrossLink.Pointer vg : particle.activeVinylGroups) {
            if (vg != vinyl) {
                result.activeVinylGroups[vinylIndex] = vg;
                vinylIndex++;
            }
        }
        result.crosslinks = new CrossLinkTree(particle.crosslinks, new CrossLink(vinyl, new CrossLink.Pointer(crosslinkSite, radIndexInParticle)));


        assert (vinylIndex == particle.activeVinylGroups.length - 1);
        if(ASSERTIONS_ENABLED) {                                                                                        //Do not enter for-loop unless assertions are enabled
            for (CrossLink c : result.crosslinks) {
                assert (result.reactiveCenters[c.firstHalf.chainNr].contains(c.firstHalf.molecule));
                assert (result.reactiveCenters[c.secondHalf.chainNr].contains(c.secondHalf.molecule));
            }
        }
        return result;
    }
}
