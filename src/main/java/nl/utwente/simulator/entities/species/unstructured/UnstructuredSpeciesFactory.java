package nl.utwente.simulator.entities.species.unstructured;

import nl.utwente.simulator.entities.RadicalPosition;
import nl.utwente.simulator.input.InputValue;
import nl.utwente.simulator.simulator.Species;
import nl.utwente.simulator.simulator.SpeciesFactory;
import nl.utwente.simulator.simulator.RejectedReaction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static nl.utwente.simulator.config.Settings.CROSSLINKER_VINYL_GROUPS;
import static nl.utwente.simulator.input.InputSource.UNSTRUCTURED;

@InputValue(value = "An unstructured particle representation which tracks radical positions", src = {UNSTRUCTURED})
public class UnstructuredSpeciesFactory implements SpeciesFactory<UnstructuredParticle> {

    @Override
    public UnstructuredParticle createI(long number) {
        return new UnstructuredParticle(number, Species.Type.I, 1, 0, 0, 0, 1, 0, 0);
    }

    @Override
    public UnstructuredParticle createM(long number) {
        return new UnstructuredParticle(number, Species.Type.M, 0, 1, 0, 1, 0, 0, 0);
    }

    @Override
    public UnstructuredParticle createC(long number) {
        return new UnstructuredParticle(number, Species.Type.C, 0, 0, 1, CROSSLINKER_VINYL_GROUPS, 0, 0, 0);
    }

    @Override
    public UnstructuredParticle createSpecies(UnstructuredParticle radical, int radIndexInParticle, UnstructuredParticle vinyl) throws RejectedReaction {
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

        return new UnstructuredParticle(1, Species.Type.POLYMER,
                radical.numberOfI + vinyl.numberOfI,
                radical.numberOfM + vinyl.numberOfM,
                radical.numberOfC + vinyl.numberOfC,
                radical.numberOfActiveVinylGroups + vinyl.numberOfActiveVinylGroups - 1,
                radical.numberOfENRadicals + vinyl.numberOfENRadicals + diffEN,
                radical.numberOfECRadicals + vinyl.numberOfECRadicals + diffEC,
                radical.numberOfMCRadicals + vinyl.numberOfMCRadicals + diffMC
        );
    }

    @Override
    public UnstructuredParticle createSpecies(UnstructuredParticle particle, int radIndexInParticle) throws RejectedReaction {
        RadicalPosition pos = particle.positionOfNthRadical(radIndexInParticle);

        switch(pos){
            case MID_CHAIN_CROSSLINKER:
                return new UnstructuredParticle(1, Species.Type.POLYMER,
                    particle.numberOfI,
                    particle.numberOfM,
                    particle.numberOfC,
                    particle.numberOfActiveVinylGroups - 1,
                    particle.numberOfENRadicals,
                    particle.numberOfECRadicals,
                    particle.numberOfMCRadicals
                );
            case CHAIN_END_CROSSLINKER:
                return new UnstructuredParticle(1, Species.Type.POLYMER,
                    particle.numberOfI,
                    particle.numberOfM,
                    particle.numberOfC,
                    particle.numberOfActiveVinylGroups - 1,
                    particle.numberOfENRadicals,
                    particle.numberOfECRadicals - 1,
                    particle.numberOfMCRadicals + 1
                );
            case CHAIN_END_NON_CROSSLINKER:
                return new UnstructuredParticle(1, Species.Type.POLYMER,
                    particle.numberOfI,
                    particle.numberOfM,
                    particle.numberOfC,
                    particle.numberOfActiveVinylGroups - 1,
                    particle.numberOfENRadicals - 1,
                    particle.numberOfECRadicals,
                    particle.numberOfMCRadicals + 1
                );
            default:
                throw new NotImplementedException();
        }
    }
}
