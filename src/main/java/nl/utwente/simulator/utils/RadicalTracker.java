package nl.utwente.simulator.utils;


import nl.utwente.simulator.simulator.Species;
import nl.utwente.simulator.utils.random.Random;

import static nl.utwente.simulator.Simulator.FIRST_POLYMER_INDEX;
import static nl.utwente.simulator.config.Settings.VESSEL_VOLUME;
import static nl.utwente.simulator.config.Settings.log;

public class RadicalTracker {


    //    k_prop         ( K  n_i*r_i*v_i        1     (     K           K            ))
    //---------------    ( ∑ ------------- + --------- ( V_T ∑ n_i*r_i - ∑ n_i*r_i*v_i))  = Sum of all reaction rates (M/s)
    //vol_T * Av * Av    ( i    vol_i          vol_T   (     i           i            ))

    //BIT:                local reactivity             global reactivity    exclusion

    DecimalFenwickTree localBIT = new DecimalFenwickTree();
    IntegerFenwickTree globalBIT = new IntegerFenwickTree();
    IntegerFenwickTree exclusionBIT = new IntegerFenwickTree();

    //K  n_i*r_i*v_i        1     (     K           K            )
    //∑ ------------- + --------- ( V_T ∑ n_i*r_i - ∑ n_i*r_i*v_i)
    //i    vol_i          vol_T   (     i           i            )
    public double totalSum(long activeVinylGroups){
        return localBIT.totalSum + (((double)(activeVinylGroups*globalBIT.totalSum)) - exclusionBIT.totalSum)/ VESSEL_VOLUME;
    }

    public int pickRadical(long activeVinylGroups){
        int index;
        double max = totalSum(activeVinylGroups);
        if (max > 0){                                                                                                   //In last steps this may become negative, due to accumulated floating point errors
            index = indexOf(Random.getRandom(max),activeVinylGroups);
        }else{
            log.warnln("[WARN] Out of bounds due to floating point error");
            index = FIRST_POLYMER_INDEX;                                                                                //In which case we return the first polymer in the molecules list
        }
        if(index >= globalBIT.size){
            index = globalBIT.size-1;
            while(globalBIT.data[index] <=0)
                index++;
        }
        return index;
    }

    private int indexOf(double partialSum, long activeVinylGroups){
        assert(partialSum>=0);
        int root = localBIT.root();
        int depth = 1;
        int pos = root;
        double sum = 0;
        while(pos % 2 == 0) {                                                                                           //while not on a leaf
            assert(pos>0);
            double newSum = sum + localBIT.data[pos]+(activeVinylGroups*globalBIT.data[pos]-exclusionBIT.data[pos])/VESSEL_VOLUME;
            if(newSum <= partialSum){                                                                                   //go right when intermediate sum < value
                pos |= root >> depth;
                sum = newSum;
            }else{                                                                                                      //go left otherwise
                pos -= root >> depth;
            }
            depth++;
        }
        if(sum + localBIT.data[pos]+(activeVinylGroups*globalBIT.data[pos]-exclusionBIT.data[pos])/VESSEL_VOLUME <= partialSum)//increase index if value is on non-leaf node
            pos++;
        return pos-1;                                                                                                   //convert result to zero-indexed
    }

    public void decreaseParticle(Species p, int index){
        localBIT.adj(index, -(p.numberOfRadicals()*p.localConcentration()));
        globalBIT.adj(index, -p.numberOfRadicals());
        exclusionBIT.adj(index, -(p.numberOfRadicals()*p.numberOfActiveVinylGroups()));
    }

    public void increaseParticle(Species p, int index){
        localBIT.adj(index, p.numberOfRadicals()*p.localConcentration());
        globalBIT.adj(index, p.numberOfRadicals());
        exclusionBIT.adj(index, p.numberOfRadicals()*p.numberOfActiveVinylGroups());
    }

    public void relocate(Species p, int oldIndex, int newIndex){
        localBIT.adj(    oldIndex, -p.number()*p.numberOfRadicals()*p.localConcentration());                            //Remove from old position
        globalBIT.adj(   oldIndex, -p.number()*p.numberOfRadicals());
        exclusionBIT.adj(oldIndex, -p.number()*p.numberOfRadicals()*p.numberOfActiveVinylGroups());

        assert(oldIndex == 0 || globalBIT.rsq(oldIndex)   == globalBIT.rsq(oldIndex-1));                                //old location now empty
        assert(oldIndex == 0 || exclusionBIT.rsq(oldIndex)== exclusionBIT.rsq(oldIndex-1));

        assert(newIndex == 0 || globalBIT.rsq(newIndex)   == globalBIT.rsq(newIndex-1));
        assert(newIndex == 0 || exclusionBIT.rsq(newIndex)== exclusionBIT.rsq(newIndex-1));

        localBIT.adj(    newIndex, p.number()*p.numberOfRadicals()*p.localConcentration());                             //Add at new position
        globalBIT.adj(   newIndex, p.number()*p.numberOfRadicals());
        exclusionBIT.adj(newIndex, p.number()*p.numberOfRadicals()*p.numberOfActiveVinylGroups());
    }

    public void halfData(){
        localBIT.halfData();
        globalBIT.halfData();
        exclusionBIT.halfData();
    }

    public void addAll(Species p){
        localBIT.add(p.number()*p.numberOfRadicals()*p.localConcentration());
        globalBIT.add(p.number()*p.numberOfRadicals());
        exclusionBIT.add(p.number()*p.numberOfRadicals()*p.numberOfActiveVinylGroups());
    }

    public void addOne(Species p){
        localBIT.add(p.numberOfRadicals()*p.localConcentration());
        globalBIT.add(p.numberOfRadicals());
        exclusionBIT.add(p.numberOfRadicals()*p.numberOfActiveVinylGroups());
    }

    public void set(Species p, int index){
        localBIT.adj(index, p.numberOfRadicals()*p.localConcentration());
        globalBIT.adj(index, p.numberOfRadicals());
        exclusionBIT.adj(index, p.numberOfRadicals()*p.numberOfActiveVinylGroups());
    }
}
