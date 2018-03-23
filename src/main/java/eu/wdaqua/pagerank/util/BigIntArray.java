package eu.wdaqua.pagerank.util;

public class BigIntArray {
    private int[][] array;

    public BigIntArray(long n){
        array = new int[segment(n)+1][];
        for (int i=0 ; i<segment(n)+1; i++){
            if (i<segment(n)){
                array[i] = new int[2147483647];
            } else {
                array[i] = new int[offset(n)];
            }
        }
    }

    public int get(long index) {
        return array[segment(index)][offset(index)];
    }

    public void set(long index, int d) {
        array[segment(index)][offset(index)]=d;
    }

    public int segment(long index){
        return (int)index/2147483647;
    }

    public int offset(long index){
        return (int)index%2147483647;
    }
}
