package nl.utwente.simulator.entities.abstractSpecies;


import nl.utwente.simulator.entities.RadicalPosition;
import nl.utwente.simulator.input.InputValue;
import nl.utwente.simulator.simulator.AbstractSpeciesFactory;
import nl.utwente.simulator.simulator.RejectedReaction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static nl.utwente.simulator.config.Settings.CROSSLINKER_VINYL_GROUPS;
import static nl.utwente.simulator.entities.abstractSpecies.CompositionalSpecies.*;
import static nl.utwente.simulator.input.InputSource.STRUCTURED;
import static nl.utwente.simulator.input.InputSource.UNSTRUCTURED;

@InputValue(value = "Composition and position of reactive centers(high detail)", src = {UNSTRUCTURED, STRUCTURED})
public class CompositionalSpeciesFactory implements AbstractSpeciesFactory<CompositionalSpecies> {

    @Override
    public CompositionalSpecies abstractSpeciesForInitiatingMolecule() {
        return new CompositionalSpecies(1, 0, 0, 0, 1, 0, 0);
    }

    @Override
    public CompositionalSpecies abstractSpeciesForMonomer() {
        return new CompositionalSpecies(0, 1, 0, 1, 0, 0, 0);
    }

    @Override
    public CompositionalSpecies abstractSpeciesForCrosslinker() {
        return new CompositionalSpecies(0, 0, 1, CROSSLINKER_VINYL_GROUPS, 0, 0, 0);
    }

    @Override
    public CompositionalSpecies abstractSpeciesAfterInterMolecularReaction(CompositionalSpecies radical, RadicalPosition pos, CompositionalSpecies vinyl) throws RejectedReaction {
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

        if(vinyl.key[I] + vinyl.key[M] + vinyl.key[C] == 1){                                                            //If not polymer
            if(vinyl.key[M] == 1){                                                                                      //If monomer
                diffEN++;
            }else{
                assert(vinyl.key[C] == 1);
                diffEC++;
            }
        }else{
            diffMC++;
        }

        assert(diffEC+diffMC+diffEN == 0);                                                                              //Radicals do not disappear

        return createGroup(
            radical.key[I] + vinyl.key[I],
            radical.key[M] + vinyl.key[M],
            radical.key[C] + vinyl.key[C],
            radical.key[V] + vinyl.key[V] - 1,
            radical.key[EN] + vinyl.key[EN] + diffEN,
            radical.key[EC] + vinyl.key[EC] + diffEC,
            radical.key[MC] + vinyl.key[MC] + diffMC
        );
    }

    @Override
    public CompositionalSpecies abstractSpeciesAfterIntraMolecularReaction(CompositionalSpecies group, RadicalPosition pos) throws RejectedReaction {
        switch(pos){
            case MID_CHAIN_CROSSLINKER:
                return createGroup(
                    group.key[I],
                    group.key[M],
                    group.key[C],
                    group.key[V] - 1,
                    group.key[EN],
                    group.key[EC],
                    group.key[MC]
                );
            case CHAIN_END_CROSSLINKER:
                return createGroup(
                        group.key[I],
                        group.key[M],
                        group.key[C],
                        group.key[V] - 1,
                        group.key[EN],
                        group.key[EC] - 1,
                        group.key[MC] + 1
                );
            case CHAIN_END_NON_CROSSLINKER:
                return createGroup(
                        group.key[I],
                        group.key[M],
                        group.key[C],
                        group.key[V] - 1,
                        group.key[EN] - 1,
                        group.key[EC],
                        group.key[MC] + 1
                );
            default:
                throw new NotImplementedException();
        }

    }

    /**
     *
     * @param i Number of (half-)initiator molecules
     * @param a Number of (non-cross-linker) monomer molecules
     * @param c Number of cross-linker molecules
     * @param p Number of pendent groups
     * @param x Number of non-cross-linker chain end reactiveCenters
     * @param y Number of cross-linker chain end reactiveCenters
     * @param m Number of cross-linker mid-chain reactiveCenters
     * @return CompositionalSpecies with the given numbers of molecules
     */
    public CompositionalSpecies createGroup(long i, long a, long c, long p, long x, long y, long m){
        return new CompositionalSpecies(i,a,c,p,x,y,m);
    }
}
