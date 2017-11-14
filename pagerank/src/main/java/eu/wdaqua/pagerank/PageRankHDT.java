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
    private static double dampingFactor = 0.85D;
    private static double startValue = 0.1D;
    private static int numberOfIterations = 40;
    private List<Integer> numberOutgoing;
    private double[] pageRankScoresShared;
    private double[] pageRankScoresObjects;

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

        pageRankScoresShared = new double[(int)hdt.getDictionary().getNshared()+1];
        pageRankScoresObjects = new double[(int)hdt.getDictionary().getNobjects()+1];


        System.out.println("nSubjects "+nSubjects);
        System.out.println("nObjects "+nObjects);
        System.out.println("nShared "+nShared);

        //Compute the number of outgoing links
        numberOutgoing = new ArrayList<Integer>();
        numberOutgoing.add(0);
        for (int id=1; id<=nSubjects; id++) {
            IteratorTripleID iteratorTripleID = hdt.getTriples().search(new TripleID(id, 0, 0));
            int count = 0;
            while (iteratorTripleID.hasNext()){
                iteratorTripleID.next();
                count++;
            }
            numberOutgoing.add(count);
        }

        //Initialize the start page rank scores
        for (int id=1; id<=nShared; id++){
            pageRankScoresShared[id]=startValue;
        }
        for (int id=1; id<=(nObjects-nShared); id++){
            pageRankScoresObjects[id]=startValue;
        }

        //Compute the page rank
        NodeDictionary nodeDictionary = new NodeDictionary(hdt.getDictionary());
        for (int j = 0; j < numberOfIterations; j++) {
            for (int id=1; id<=nObjects; id++){
                //Don't do pagerank for literal only for
                boolean isLiteral = false;
                if (id>nShared){
                    Node node = nodeDictionary.getNode(id,TripleComponentRole.OBJECT);
                    isLiteral = node.isLiteral();
                }
                if (!isLiteral){
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
                        long numberOut = numberOutgoing.get(inLink.getSubject());
                        System.out.println("dampingFactor "+dampingFactor);
                        System.out.println("pageRankIn "+pageRankIn);
                        System.out.println("numberOut "+numberOut);
                        pageRank += dampingFactor * (pageRankIn / numberOut);
                    }
                    if (id<=nShared){
                        pageRankScoresShared[id]=pageRank;
                        System.out.println(hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT) + " " +pageRank);
                    } else {
                        pageRankScoresObjects[id-nShared]=pageRank;
                        System.out.println("here "+(id-nShared));
                        System.out.println(hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT) + " " +pageRank);
                    }

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
        for (int id=1; id<=(nObjects-nShared); id++){
            Score s = new Score();
            s.node = hdt.getDictionary().idToString(id+nShared,TripleComponentRole.OBJECT).toString();
            s.pageRank =  pageRankScoresObjects[id];
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
        for (int id=1; id<=(nObjects-nShared); id++){
            System.out.println(hdt.getDictionary().idToString(id+nShared,TripleComponentRole.OBJECT) + "\t" + pageRankScoresObjects[id]);
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
