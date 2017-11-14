import eu.wdaqua.pagerank.PageRankHDT;

import org.junit.Test;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTSpecification;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class SmallTest implements ProgressListener{
    @Test
    public void go1(){
        System.out.println("Checking first example");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example1.n3").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.50, 1.0 , 20);
        pageRankHDT.compute();

        List<PageRankHDT.Score> scores= pageRankHDT.getPageRankScores();
        HashMap<String,Double> result = new HashMap<String, Double>();
        result.put("PageA", 1.07692308);
        result.put("PageB", 0.76923077);
        result.put("PageC", 1.15384615);
        for (PageRankHDT.Score score : scores){
            assertEquals(result.get(score.node), score.pageRank, 0.001);
        }
    }

    @Test
    public void go2(){
        System.out.println("Checking second example");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example2.n3").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.85, 0.15 , 20);
        pageRankHDT.compute();

        List<PageRankHDT.Score> scores= pageRankHDT.getPageRankScores();
        HashMap<String,Double> result = new HashMap<String, Double>();
        result.put("PageA", 1.49);
        result.put("PageB", 0.78);
        result.put("PageC", 1.58);
        result.put("PageD", 0.15);
        for (PageRankHDT.Score score : scores){
            assertEquals(result.get(score.node), score.pageRank, 0.01);
        }
    }

    @Override
    public void notifyProgress(float level, String message) {
        if(false) {
            System.out.print("\r"+message + "\t"+ Float.toString(level)+"                            \r");
        }
    }

}
