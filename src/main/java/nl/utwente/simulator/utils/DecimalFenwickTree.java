package nl.utwente.simulator.utils;

import static nl.utwente.simulator.config.Settings.log;

public class DecimalFenwickTree {                                                                                       //No generics, we don't want boxing and unboxing

    protected double[] data;
    public int size;
    public double totalSum;

    /**
     * Creates a binary tree of depth <code>depth</code>
     * of which the zero-th element is used as a buffer
     * This tree can thus contain (2^<code>depth</code>)-2 elements
     * @param depth depth of this tree
     */
    public DecimalFenwickTree(int depth){
        int size = (int) Math.pow(2, depth);
        data = new double[size];                                                                                        //1-indexed, so we need another place for the 0
        this.size=0;
        this.totalSum=0;
    }

    public DecimalFenwickTree(){
        data = new double[1];                                                                                           //1-indexed, so we need another place for the 0
        this.size=0;
        this.totalSum=0;
    }

    public int root(){
        return data.length/2;
    }

    public double node(int index){
        return data[index];
    }

    public int indexOf(double prob){
        int mask = data.length/2;
        int index = mask;

        double sum = 0;                                                                                                 //slight variation of Fenwick's algorithm
        while(mask > 1) {
            mask /= 2;
            double newSum = sum + data[index];
            if(prob >= newSum){                                                                                         //go right when intermediate sum < value
                index |= mask;
                sum = newSum;
            }else{                                                                                                      //go left otherwise
                index -= mask;
            }
        }

        if(sum + data[index] <= prob)                                                                                   //increase index if value is on non-leaf node
            index++;
        assert(index-1<size);
        return index-1;                                                                                                 //convert result to zero-indexed
    }

    public void add(double value){
        if(size >= data.length-1){
            doubleData();
        }
        adjust(size+1, value);
        totalSum+=value;
        size++;
    }

    public void adj(int index, double value){
        index++;                                                                                                        //1-indexed
        assert(index < data.length);
        adjust(index, value);
        totalSum+=value;
    }

    public double rsq(int b){
        b++;                                                                                                            //1-indexed
        double sum = 0;
        for(;b>0;b-=(b & -b)) sum += data[b];
        return sum;
    }

    private void adjust(int k, double v){
        for(;k<data.length;k+= (k & -k)){
            data[k] += v;
        }
    }

    private void doubleData(){
        double[] tmp = new double[2*data.length];
        for(int i=0;i<data.length;i++){
            tmp[i] = data[i];
        }
        tmp[data.length] = totalSum;
        log.debugln("Doubling Fenwick tree");
        data = tmp;
    }

    public void halfData(){
        double[] tmp = new double[data.length/2];
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
