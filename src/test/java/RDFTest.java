import eu.wdaqua.pagerank.PageRankRDF;
import eu.wdaqua.pagerank.PageRankScore;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class RDFTest {

    @Test
    public void go1(){
        System.out.println("Checking example 1 RDF");
        ClassLoader classLoader = getClass().getClassLoader();
        String dump = classLoader.getResource("example1.nt").getFile();
        for (boolean b : Arrays.asList(true,false)) {
            PageRankRDF pageRankRDF = new PageRankRDF(dump, 0.50, 1.0, 40,false,b);
            pageRankRDF.compute();

            List<PageRankScore> scores = pageRankRDF.getPageRankScores();
            HashMap<String, Double> result = new HashMap<String, Double>();
            result.put("PageA", 1.07692308);
            result.put("PageB", 0.76923077);
            result.put("PageC", 1.15384615);
            for (PageRankScore score : scores) {
                assertEquals(result.get(score.node), score.pageRank, 0.001);
            }
        }
    }

    @Test
    public void go2(){
        System.out.println("Checking example 2 RDF");
        ClassLoader classLoader = getClass().getClassLoader();
        String dump = classLoader.getResource("example2.nt").getFile();
        for (boolean b : Arrays.asList(true,false)) {
            PageRankRDF pageRankRDF = new PageRankRDF(dump, 0.85, 0.15, 40,false,b);
            pageRankRDF.compute();

            List<PageRankScore> scores = pageRankRDF.getPageRankScores();
            HashMap<String, Double> result = new HashMap<String, Double>();
            result.put("PageA", 1.49);
            result.put("PageB", 0.78);
            result.put("PageC", 1.58);
            result.put("PageD", 0.15);
            for (PageRankScore score : scores) {
                assertEquals(result.get(score.node), score.pageRank, 0.01);
            }
        }
    }

    @Test
    public void go3(){
        System.out.println("Checking example 3 RDF");
        ClassLoader classLoader = getClass().getClassLoader();
        String dump = classLoader.getResource("example3.nt").getFile();
        for (boolean b : Arrays.asList(true,false)) {
            PageRankRDF pageRankRDF = new PageRankRDF(dump, 0.85, 0.15, 40,false,b);
            pageRankRDF.compute();

            List<PageRankScore> scores = pageRankRDF.getPageRankScores();
            HashMap<String, Double> result = new HashMap<String, Double>();
            result.put("PageA", 1.49);
            result.put("PageB", 0.78);
            result.put("PageC", 1.58);
            result.put("PageD", 0.15);
            for (PageRankScore score : scores) {
                assertEquals(result.get(score.node), score.pageRank, 0.01);
            }
        }
    }

    @Test
    public void go4(){
        System.out.println("Checking example 4 RDF");
        ClassLoader classLoader = getClass().getClassLoader();
        String dump = classLoader.getResource("example4.nt").getFile();
        for (boolean b : Arrays.asList(true,false)) {
            PageRankRDF pageRankRDF = new PageRankRDF(dump, 0.85, 0.15, 40, false, b);
            pageRankRDF.compute();

            List<PageRankScore> scores = pageRankRDF.getPageRankScores();
            HashMap<String, Double> result = new HashMap<String, Double>();
            result.put("PageA", 0.92);
            result.put("PageB", 0.41);
            result.put("PageC", 0.41);
            result.put("PageD", 0.41);
            result.put("PageE", 0.22);
            result.put("PageF", 0.22);
            result.put("PageG", 0.22);
            result.put("PageH", 0.22);
            for (PageRankScore score : scores) {
                assertEquals(result.get(score.node), score.pageRank, 0.01);
            }
        }
    }

    @Test
    public void go5(){
        System.out.println("Checking example 5 RDF");
        ClassLoader classLoader = getClass().getClassLoader();
        String dump = classLoader.getResource("example5.nt").getFile();
        for (boolean b : Arrays.asList(true,false)) {
            PageRankRDF pageRankRDF = new PageRankRDF(dump, 0.85, 0.15, 40, false, b);
            pageRankRDF.compute();

            List<PageRankScore> scores = pageRankRDF.getPageRankScores();
            HashMap<String, Double> result = new HashMap<String, Double>();
            result.put("PageA", 0.92);
            result.put("PageB", 0.41);
            result.put("PageC", 0.41);
            result.put("PageD", 0.41);
            result.put("PageE", 0.22);
            result.put("PageF", 0.22);
            result.put("PageG", 0.22);
            result.put("PageH", 0.22);
            for (PageRankScore score : scores) {
                assertEquals(result.get(score.node), score.pageRank, 0.01);
            }
        }
    }

    @Test
    public void go7(){
        System.out.println("Checking example 7 RDF");
        ClassLoader classLoader = getClass().getClassLoader();
        String dump = classLoader.getResource("example7.nt").getFile();
        for (boolean b : Arrays.asList(true,false)){
            PageRankRDF pageRankRDF = new PageRankRDF(dump, 0.85, 0.15 , 40,true, b);
            pageRankRDF.compute();

            List<PageRankScore> scores= pageRankRDF.getPageRankScores();
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
                assertEquals(result.get(score.node), score.pageRank, 0.01);
            }
        }

    }
}
