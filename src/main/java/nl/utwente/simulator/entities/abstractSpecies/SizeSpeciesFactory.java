package nl.utwente.simulator.entities.abstractSpecies;


import nl.utwente.simulator.entities.RadicalPosition;
import nl.utwente.simulator.input.InputValue;
import nl.utwente.simulator.simulator.AbstractSpeciesFactory;
import nl.utwente.simulator.simulator.RejectedReaction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static nl.utwente.simulator.config.Settings.CROSSLINKER_VINYL_GROUPS;
import static nl.utwente.simulator.entities.abstractSpecies.SizeSpecies.*;
import static nl.utwente.simulator.input.InputSource.STRUCTURED;
import static nl.utwente.simulator.input.InputSource.UNSTRUCTURED;

@InputValue(value = "Size and position of reactive centers(low detail)", src = {UNSTRUCTURED, STRUCTURED})
public class SizeSpeciesFactory implements AbstractSpeciesFactory<SizeSpecies> {

    @Override
    public SizeSpecies abstractSpeciesForInitiatingMolecule() {
        return new SizeSpecies(1, 0, 1, 0, 0);
    }

    @Override
    public SizeSpecies abstractSpeciesForMonomer() {
        return new SizeSpecies(1, 1, 0, 0, 0);
    }

    @Override
    public SizeSpecies abstractSpeciesForCrosslinker() {
        return new SizeSpecies(1, CROSSLINKER_VINYL_GROUPS, 0, 0, 0);
    }

    @Override
    public SizeSpecies abstractSpeciesAfterInterMolecularReaction(SizeSpecies radical, RadicalPosition pos, SizeSpecies vinyl) throws RejectedReaction {
        byte diffEC = 0;
        byte diffEN = 0;
        byte diffMC = 0;

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

        if(vinyl.key[SIZE] == 1){                                                            //If not polymer
            if(vinyl.key[V] == 1){                                                                                      //If monomer
                diffEN++;
            }else{
                assert(vinyl.key[V] == CROSSLINKER_VINYL_GROUPS);
                diffEC++;
            }
        }else{
            diffMC++;
        }

        assert(diffEC+diffMC+diffEN == 0);                                                                              //Radicals do not disappear

        return new SizeSpecies(
                radical.key[SIZE] + vinyl.key[SIZE],
                radical.key[V] + vinyl.key[V] - 1,
                radical.key[EN] + vinyl.key[EN] + diffEN,
                radical.key[EC] + vinyl.key[EC] + diffEC,
                radical.key[MC] + vinyl.key[MC] + diffMC
        );
    }

    @Override
    public SizeSpecies abstractSpeciesAfterIntraMolecularReaction(SizeSpecies group, RadicalPosition pos) throws RejectedReaction {
        switch(pos){
            case MID_CHAIN_CROSSLINKER:
                return new SizeSpecies(
                        group.key[SIZE],
                        group.key[V] - 1,
                        group.key[EN],
                        group.key[EC],
                        group.key[MC]
                );
            case CHAIN_END_CROSSLINKER:
                return new SizeSpecies(
                        group.key[SIZE],
                        group.key[V] - 1,
                        group.key[EN],
                        group.key[EC] - 1,
                        group.key[MC] + 1
                );
            case CHAIN_END_NON_CROSSLINKER:
                return new SizeSpecies(
                        group.key[SIZE],
                        group.key[V] - 1,
                        group.key[EN] - 1,
                        group.key[EC],
                        group.key[MC] + 1
                );
            default:
                throw new NotImplementedException();
        }

    }

}
