package eu.wdaqua.pagerank;

import org.apache.jena.graph.Node;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;

import org.rdfhdt.hdtjena.NodeDictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PageRankHDT {

    private HDT hdt;
    int nShared;
    int nSubjects;
    int nObjects;
    int numberNonLiterals;
    private static double dampingFactor = 0.85D;
    private static double startValue = 0.1D;
    private static int numberOfIterations = 40;
    private int[] numberOutgoing;
    private double[] pageRankScoresShared;
    private double[] pageRankScoresObjects;
    List<Integer> indexesNonLiterals = new ArrayList<>();

    PageRankHDT(String hdtDump){
        this.load(hdtDump);
    }

    PageRankHDT(String hdtDump, double dampingFactor, double startValue,  int numberOfIterations){
        this.load(hdtDump);
        this.dampingFactor = dampingFactor;
        this.startValue = startValue;
        this.numberOfIterations = numberOfIterations;
    }

    public PageRankHDT(HDT hdt, double dampingFactor, double startValue, int numberOfIterations){
        this.hdt = hdt;
        nShared = (int) hdt.getDictionary().getNshared();
        nSubjects = (int) hdt.getDictionary().getNsubjects();
        nObjects= (int) hdt.getDictionary().getNobjects();
        this.dampingFactor = dampingFactor;
        this.startValue = startValue;
        this.numberOfIterations = numberOfIterations;
    }

    void load(String hdtDump){
        try {
            hdt = HDTManager.mapIndexedHDT(hdtDump, null);
            nShared = (int) hdt.getDictionary().getNshared();
            nSubjects = (int) hdt.getDictionary().getNsubjects();
            nObjects= (int) hdt.getDictionary().getNobjects();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compute() {
        System.out.println("Computing eu.wdaqua.pagerank.PageRank: " + numberOfIterations +
                " iterations, damping factor " + dampingFactor +
                ", start value " + startValue);


        System.out.println("nSubjects "+nSubjects);
        System.out.println("nObjects "+nObjects);
        System.out.println("nShared "+nShared);

        //Save indeces which are not literals, don't do pagerank for literal only for not uris
        NodeDictionary nodeDictionary = new NodeDictionary(hdt.getDictionary());
        numberNonLiterals = 0;
        for (int id=1; id<=nObjects; id++){
            boolean isLiteral = false;
            if (id>nShared){
                Node node = nodeDictionary.getNode(id,TripleComponentRole.OBJECT);
                isLiteral = node.isLiteral();
                if (!isLiteral) {
                    indexesNonLiterals.add(id);
                    numberNonLiterals++;
                }
            }
        }
        System.out.println("NUMBER "+numberNonLiterals);

        pageRankScoresShared = new double[(int)hdt.getDictionary().getNshared()+1];
        pageRankScoresObjects = new double[numberNonLiterals];

        //Initialize the start page rank scores
        for (int id=1; id<=nShared; id++){
            pageRankScoresShared[id]=startValue;
        }
        for (int k=0; k<numberNonLiterals; k++){
            pageRankScoresObjects[k]=startValue;
        }

        //Compute the number of outgoing links
        numberOutgoing = new int[(int)hdt.getDictionary().getNsubjects()+1];
        numberOutgoing[0]=0;
        for (int id=1; id<=nSubjects; id++) {
            IteratorTripleID iteratorTripleID = hdt.getTriples().search(new TripleID(id, 0, 0));
            int count = 0;
            while (iteratorTripleID.hasNext()){
                Node node = nodeDictionary.getNode(iteratorTripleID.next().getObject(),TripleComponentRole.OBJECT);
                if (!node.isLiteral()){
                    count++;
                }
            }
            numberOutgoing[id]=count;
        }

        //Compute the page rank
        for (int j = 0; j < numberOfIterations; j++) {
            for (int k=1; k<=nShared+numberNonLiterals; k++){
                int id = k;
                if (k>nShared){
                    System.out.println("ID "+k);
                    id = indexesNonLiterals.get(k-nShared-1);
                }
                TripleID root = new TripleID(0, 0,id);
                IteratorTripleID incomingLinks= hdt.getTriples().search(root);
                double pageRank = 1.0D - dampingFactor;
                while (incomingLinks.hasNext()) {
                    TripleID inLink = incomingLinks.next();
                    Double pageRankIn = 0.0;
                    if (inLink.getSubject()<=nShared){
                        pageRankIn = pageRankScoresShared[inLink.getSubject()];
                    } else {
                        pageRankIn = startValue;
                    }
                    long numberOut = numberOutgoing[inLink.getSubject()];
//                    System.out.println("dampingFactor "+dampingFactor);
//                    System.out.println("pageRankIn "+pageRankIn);
//                    System.out.println("numberOut "+numberOut);
                    pageRank += dampingFactor * (pageRankIn / numberOut);
                }
                if (id<=nShared){
                    pageRankScoresShared[id]=pageRank;
                    System.out.println(hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT) + " " +pageRank);
                } else {
                    pageRankScoresObjects[k-nShared-1]=pageRank;
                    System.out.println(hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT) + " " +pageRank);
                }
            }

        }
    }

    public List<Score> getPageRankScores() {
        List<Score> scores = new ArrayList<Score>();
        for (int id=1; id<=nShared; id++){
            Score s = new Score();
            s.node = hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT).toString();
            s.pageRank =  pageRankScoresShared[id];
            scores.add(s);
        }
        for (int k=0; k<numberNonLiterals; k++){
            int id = indexesNonLiterals.get(k);
            System.out.println(id);
            Score s = new Score();
            s.node = hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT).toString();
            s.pageRank =  pageRankScoresObjects[k];
            scores.add(s);
        }
        for (int id=1; id<=(nSubjects-nShared); id++){
            Score s = new Score();
            s.node = hdt.getDictionary().idToString(id+nShared,TripleComponentRole.SUBJECT).toString();
            s.pageRank =  startValue;
            scores.add(s);
        }
        return scores;

    }

    public void printPageRankScores(){
        for (int id=1; id<=nShared; id++){
            System.out.println(hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT) + "\t" + pageRankScoresShared[id]);
        }
        for (int k=0; k<numberNonLiterals; k++){
            int id = indexesNonLiterals.get(k);
            System.out.println(hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT) + "\t" + pageRankScoresObjects[k]);
        }
        for (int id=1; id<=(nSubjects-nShared); id++){
            System.out.println(hdt.getDictionary().idToString(id+nShared,TripleComponentRole.SUBJECT) + "\t" + startValue);
        }
    }

    public class Score{
        public String node;
        public Double pageRank;
    }

}
