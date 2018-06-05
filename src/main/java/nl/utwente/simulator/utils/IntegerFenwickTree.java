package nl.utwente.simulator.utils;

import static nl.utwente.simulator.config.Settings.log;

public class IntegerFenwickTree {                                                                                       //No generics, we don't want boxing and unboxing

    protected long[] data;
    public int size;

    /**
     * We track the total sum separately to prevent adding an extra layer of depth to the BIT
     */
    public long totalSum;

    /**
     * Creates a binary tree of depth <code>depth</code>
     * of which the zero-th element is used as a buffer
     * This tree can thus contain (2^<code>depth</code>)-2 elements
     * @param depth depth of this tree
     */
    public IntegerFenwickTree(int depth){
        int size = (int) Math.pow(2, depth);
        data = new long[size];                                                                                          //1-indexed, so we need another place for the 0
        this.size=0;
        this.totalSum=0;
    }

    public IntegerFenwickTree(){
        data = new long[1];                                                                                             //1-indexed, so we need another place for the 0
        this.size=0;
        this.totalSum=0;
    }

    public int root(){
        return data.length/2;
    }

    public long node(int index){
        return data[index];
    }

    public int indexOf(long prob){
        prob++;                                                                                                         //convert input to 1-indexed (expression.g. first 8 values have indices 1..8 instead of 0..7)

        int mask = data.length/2;
        int index = 0;
        int textIx;
        if(prob > data[0]){
            while(mask != 0){
                textIx = index + mask;
                if(prob > data[textIx]){                                                                                //replace <= with < to correct for 1-indexing
                    index = textIx;
                    prob -= data[index];
                }
                mask /=2;
            }
        }
        return index;
    }

    public void add(long value){
        if(size >= data.length-1){
            doubleData();
        }
        adjust(size+1, value);
        totalSum+=value;
        size++;
        assert(totalSum == rsq(data.length-2));
    }

    public void adj(int index, long value){
        index++;                                                                                                        //1-indexed
        assert(index < data.length);
        adjust(index, value);
        totalSum+=value;
        assert(totalSum == rsq(data.length-2));                                                                         //extra -1 to compensate for 1-indexing
    }

    public long rsq(int b){
        b++;                                                                                                            //1-indexed
        long sum = 0;
        for(;b>0;b-=(b & -b)) sum += data[b];
        return sum;
    }

    private void adjust(int k, long v){
        for(;k<data.length;k+= (k & -k)){
            data[k] += v;
            assert(data[k] >= 0);
        }
    }

    private void doubleData(){
        long[] tmp = new long[2*data.length];
        for(int i=0;i<data.length;i++){
            tmp[i] = data[i];
        }
        tmp[data.length] = totalSum;
        log.debugln("Doubling Fenwick tree");
        data = tmp;
    }

    public void halfData(){
        assert(rsq((data.length/2) -1)== rsq(size-1));
        long[] tmp = new long[data.length/2];
        for(int i=0;i<data.length/2;i++){
            tmp[i] = data[i];
        }
        log.debugln("Halving Fenwick tree");
        data = tmp;
        size = data.length-1;
    }

    @Override
    public String toString(){
        String r = "VALS: ("+data[0]+"), ";
        for(int i=1;i<data.length;i++){
            r +=data[i]+", ";
        }

        r+="\nRSQ:";
        for(int i=0;i<size;i++) {
            r+=rsq(i)+", ";
        }
        return r;
    }

    public int subtreeSize(){
        return (data.length/2) -1;
    }
}
