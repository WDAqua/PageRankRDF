package eu.wdaqua.pagerank.util;

import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.util.string.CharSequenceComparator;

// Small util to find literals in the dictionary using binary search

public class BinarySearch {
    private static CharSequenceComparator comparator = new CharSequenceComparator();

    //FIRST occurrence of " in the dictionary between the indexes low and high, if not found return -1
    public static long first(Dictionary dictionary, long low, long high) {
        if (high >= low) {
            long mid = low + (high - low) / 2;
//            System.out.println("low "+low);
//            System.out.println("high "+high);
//            System.out.println("mid "+mid);
            int c = -1;
            if (mid!=1){
                c = comparator.compare("\"", dictionary.idToString(mid - 1, TripleComponentRole.OBJECT).toString().subSequence(0, 1));
            }
            int c2 = comparator.compare("\"", dictionary.idToString(mid, TripleComponentRole.OBJECT).toString().subSequence(0, 1));
//            System.out.println("c"+c);
//            System.out.println("c2 "+c2);
            if ((mid == 1 || c < 0) && c2 == 0)
                return mid;
            else if (c > 0)
                return first(dictionary, (mid + 1), high);
            else
                return first(dictionary, low, (mid - 1));
        }
        return -1;
    }

    //LAST occurrence of " in the dictionary between the indexes low and high, if not found return -1
    public static long last(Dictionary dictionary, long low, long high, long n) {
        if (high >= low) {
            long mid = low + (high - low) / 2;
//            System.out.println("low "+low);
//            System.out.println("high "+high);
//            System.out.println("mid "+mid);
            int c = -1;
            if (mid!=n) {
                c = comparator.compare("\"", dictionary.idToString(mid + 1, TripleComponentRole.OBJECT).toString().subSequence(0, 1));
            }
            int c2 = comparator.compare("\"", dictionary.idToString(mid, TripleComponentRole.OBJECT).toString().subSequence(0, 1));
//            System.out.println("c"+c);
//            System.out.println("c2 "+c2);
            if ((mid == n || c < 0) && c2 ==0)
                return mid;
            else if (c < 0)
                return last(dictionary, low, (mid - 1), n);
            else
                return last(dictionary, (mid + 1), high, n);
        }
        return -1;
    }
}