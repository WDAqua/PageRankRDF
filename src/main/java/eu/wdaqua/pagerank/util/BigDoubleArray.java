package eu.wdaqua.pagerank.util;

public class BigDoubleArray {
    private double[][] array;

    public BigDoubleArray(long n){
        array = new double[segment(n)+1][];
        for (int i=0 ; i<segment(n)+1; i++){
            if (i<segment(n)){
                array[i] = new double[2147483647];
            } else {
                array[i] = new double[offset(n)];
            }
        }
    }

    public double get(long index) {
        return array[segment(index)][offset(index)];
    }

    public void set(long index, double d) {
        array[segment(index)][offset(index)]=d;
    }

    public int segment(long index){
        return (int)index/2147483647;
    }

    public int offset(long index){
        return (int)index%2147483647;
    }
}
