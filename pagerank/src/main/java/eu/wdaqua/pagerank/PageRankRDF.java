package eu.wdaqua.pagerank;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.PipedRDFIterator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PageRankRDF implements PageRank{

    private static double dampingFactor = 0.85D;
    private static double startValue = 0.1D;
    private static int numberOfIterations = 40;
    private String dump;
    private HashMap<String, Double> pageRankScores = new HashMap();

    public PageRankRDF(String dump){
        this.dump = dump;
    }

    public PageRankRDF(String dump, double dampingFactor, double startValue, int numberOfIterations){
        this.dump = dump;
        this.dampingFactor = dampingFactor;
        this.startValue = startValue;
        this.numberOfIterations = numberOfIterations;
    }

    public void compute() {

        //Compute the number of outgoing edges
        HashMap<String, Integer> numberOutgoing = new HashMap();
        HashMap<String, ArrayList<String>> incomingPerPage = new HashMap<String, ArrayList<String>>();
        long time = System.currentTimeMillis();
        PipedRDFIterator<Triple> iter = Parser.parse(dump);
        while (iter.hasNext()) {
            Triple t = iter.next();
            if (t.getObject().isURI()){
                ArrayList<String> incoming = (ArrayList)((HashMap)incomingPerPage).get(t.getObject().toString());
                if (incoming == null)
                {
                    incoming = new ArrayList();
                    ((HashMap)incomingPerPage).put(t.getObject().toString(), incoming);
                }
                ArrayList<String> incoming2 = (ArrayList)((HashMap)incomingPerPage).get(t.getSubject().toString());
                if (incoming2 == null) {
                    ((HashMap)incomingPerPage).put(t.getSubject().toString(), new ArrayList());
                }
                incoming.add(t.getSubject().toString());
                Integer numberOut = (Integer)numberOutgoing.get(t.getSubject().toString());
                if (numberOut == null) {
                    numberOutgoing.put(t.getSubject().toString(), Integer.valueOf(1));
                } else {
                    numberOutgoing.put(t.getSubject().toString(), Integer.valueOf(numberOut.intValue() + 1));
                }
            }
        }
        iter.close();
        time = System.currentTimeMillis() - time;
        System.err.println("Reading input took " + time / 1000L + "s");
        time = System.currentTimeMillis();


        System.err.println("Computing PageRank: " + numberOfIterations +
                " iterations, damping factor " + dampingFactor +
                ", start value " + startValue);

        Set<String> keyset = incomingPerPage.keySet();
        System.err.println("Iteration ...");
        for (int j = 1; j <= numberOfIterations; j++) {
            System.err.print(j +" ");
            for (String string : keyset) {
                ArrayList<String> incomingLinks = (ArrayList)incomingPerPage.get(string);

                double pageRank = 1.0D - dampingFactor;
                for (String inLink : incomingLinks) {
                    Double pageRankIn = (Double)pageRankScores.get(inLink);
                    if (pageRankIn == null) {
                        pageRankIn = Double.valueOf(startValue);
                    }
                    int numberOut = ((Integer)numberOutgoing.get(inLink)).intValue();
                    pageRank += dampingFactor * (pageRankIn.doubleValue() / numberOut);
                }
                pageRankScores.put(string, Double.valueOf(pageRank));
            }
        }
        System.err.println();



        time = System.currentTimeMillis() - time;
        System.err.println("Computing PageRank took " + time / 1000L + "s");
    }

    public List<PageRankScore> getPageRankScores() {
        List<PageRankScore> scores = new ArrayList<PageRankScore>();
        Set<String> keysetNew = pageRankScores.keySet();
        for (String string : keysetNew) {
            PageRankScore s = new PageRankScore();
            s.node = string;
            s.pageRank = pageRankScores.get(string);
            scores.add(s);
        }
        return scores;
    }

    public void printPageRankScoresTSV(PrintWriter writer){
        List<PageRankScore> scores = new ArrayList<PageRankScore>();
        Set<String> keysetNew = pageRankScores.keySet();
        for (String string : keysetNew) {
            writer.println(string + "\t" + pageRankScores.get(string));
        }
    }

    public void printPageRankScoresRDF(PrintWriter writer){
        List<PageRankScore> scores = new ArrayList<PageRankScore>();
        Set<String> keysetNew = pageRankScores.keySet();
        for (String string : keysetNew) {
            writer.println("<"+string+"> <http://purl.org/voc/vrank#hasRank>\t [<http://purl.org/voc/vrank#rankValue>\t\""+pageRankScores.get(string)+"\"^^<http://www.w3.org/2001/XMLSchema#float>] .");
        }
    }
}
