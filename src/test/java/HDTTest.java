import eu.wdaqua.pagerank.PageRankHDT;
import eu.wdaqua.pagerank.PageRankScore;

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


public class HDTTest implements ProgressListener{
    @Test
    public void go1(){
        System.out.println("Checking example 1 HDT");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example1.nt").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "www.wdaqua.eu/qa", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.50, 1.0 , 40);
        pageRankHDT.compute();

        List<PageRankScore> scores= pageRankHDT.getPageRankScores();
        HashMap<String,Double> result = new HashMap<String, Double>();
        result.put("PageA", 1.07692308);
        result.put("PageB", 0.76923077);
        result.put("PageC", 1.15384615);
        for (PageRankScore score : scores){
            assertEquals(result.get(score.node), score.pageRank, 0.001);
        }
    }

    @Test
    public void go2(){
        System.out.println("Checking example 2 HDT");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example2.nt").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "www.wdaqua.eu/qa", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.85, 0.15 , 40);
        pageRankHDT.compute();

        List<PageRankScore> scores= pageRankHDT.getPageRankScores();
        HashMap<String,Double> result = new HashMap<String, Double>();
        result.put("PageA", 1.49);
        result.put("PageB", 0.78);
        result.put("PageC", 1.58);
        result.put("PageD", 0.15);
        for (PageRankScore score : scores){
            assertEquals(result.get(score.node), score.pageRank, 0.01);
        }
    }

    @Test
    public void go3(){
        System.out.println("Checking example 3 HDT");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example3.nt").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "www.wdaqua.eu/qa", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.85, 0.15 , 40);
        pageRankHDT.compute();

        List<PageRankScore> scores= pageRankHDT.getPageRankScores();
        HashMap<String,Double> result = new HashMap<String, Double>();
        result.put("PageA", 1.49);
        result.put("PageB", 0.78);
        result.put("PageC", 1.58);
        result.put("PageD", 0.15);
        for (PageRankScore score : scores){
            assertEquals(result.get(score.node), score.pageRank, 0.01);
        }
    }

    @Test
    public void go4(){
        System.out.println("Checking example 4 HDT");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example4.nt").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "www.wdaqua.eu/qa", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.85, 0.15 , 40);
        pageRankHDT.compute();

        List<PageRankScore> scores= pageRankHDT.getPageRankScores();
        HashMap<String,Double> result = new HashMap<String, Double>();
        result.put("PageA", 0.92);
        result.put("PageB", 0.41);
        result.put("PageC", 0.41);
        result.put("PageD", 0.41);
        result.put("PageE", 0.22);
        result.put("PageF", 0.22);
        result.put("PageG", 0.22);
        result.put("PageH", 0.22);

        for (PageRankScore score : scores){
            assertEquals(result.get(score.node), score.pageRank, 0.01);
        }
    }

    @Test
    public void go5(){
        System.out.println("Checking example 5 HDT");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example5.nt").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "www.wdaqua.eu/qa", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.85, 0.15 , 40);
        pageRankHDT.compute();

        List<PageRankScore> scores= pageRankHDT.getPageRankScores();
        HashMap<String,Double> result = new HashMap<String, Double>();
        result.put("PageA", 0.92);
        result.put("PageB", 0.41);
        result.put("PageC", 0.41);
        result.put("PageD", 0.41);
        result.put("PageE", 0.22);
        result.put("PageF", 0.22);
        result.put("PageG", 0.22);
        result.put("PageH", 0.22);

        for (PageRankScore score : scores){
            assertEquals(result.get(score.node), score.pageRank, 0.01);
        }
    }

    @Test
    public void go6(){
        System.out.println("Checking example 6 HDT");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example6.nt").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "www.wdaqua.eu/qa", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.85, 0.15 , 40);
        pageRankHDT.compute();
    }

    @Test
    public void go7(){
        System.out.println("Checking example 7 HDT");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example7.nt").getFile());
        HDT hdt = null;
        try {
            hdt = HDTManager.generateHDT(file.getAbsolutePath(), "www.wdaqua.eu/qa", RDFNotation.NTRIPLES, new HDTSpecification(), this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        PageRankHDT pageRankHDT = new PageRankHDT(hdt, 0.85, 0.15 , 40, true, false);
        pageRankHDT.compute();

        List<PageRankScore> scores= pageRankHDT.getPageRankScores();
        HashMap<String,Double> result = new HashMap<String, Double>();
        result.put("PageA", 0.92);
        result.put("PageB", 0.41);
        result.put("PageC", 0.41);
        result.put("PageD", 0.41);
        result.put("PageE", 0.22);
        result.put("PageF", 0.22);
        result.put("PageG", 0.22);
        result.put("\"literal 1\"", 0.22);

        for (PageRankScore score : scores){
            System.out.println(score.node);
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
