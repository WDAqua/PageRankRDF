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
import java.util.List;

public class PageRankHDT {

    private static double dampingFactor = 0.85D;
    private static double startValue = 0.1D;
    private static int numberOfIterations = 40;
    private HDT hdt;
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
        this.dampingFactor = dampingFactor;
        this.startValue = startValue;
        this.numberOfIterations = numberOfIterations;
    }

    void load(String hdtDump){
        try {
            hdt = HDTManager.mapIndexedHDT(hdtDump, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compute() {
        System.out.println("Computing eu.wdaqua.pagerank.PageRank: " + numberOfIterations +
                " iterations, damping factor " + dampingFactor +
                ", start value " + startValue);

        int nShared = (int) hdt.getDictionary().getNshared();
        int nSubjects = (int) hdt.getDictionary().getNsubjects();
        int nObjects= (int) hdt.getDictionary().getNobjects();

        System.out.println("Initialize the page rank scores");

        //Save indeces which are not literals, don't do pagerank for literal only for not uris
        NodeDictionary nodeDictionary = new NodeDictionary(hdt.getDictionary());
        int numberNonLiterals = 0;
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
        System.out.println("Compute number of outgoing edges");
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
        System.out.println("Compute the page rank");
        System.err.println("Iteration...");
        for (int j = 0; j < numberOfIterations; j++) {
            System.err.print(j +" ");
            for (int k=1; k<=nShared+numberNonLiterals; k++){
                int id = k;
                if (k>nShared){
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
                    pageRank += dampingFactor * (pageRankIn / numberOut);
                }
                if (id<=nShared){
                    pageRankScoresShared[id]=pageRank;
                } else {
                    pageRankScoresObjects[k-nShared-1]=pageRank;
                }
            }
        }
        System.out.println("\n");
    }

    public List<Score> getPageRankScores() {
        int nShared = (int) hdt.getDictionary().getNshared();
        int nSubjects = (int) hdt.getDictionary().getNsubjects();
        int nObjects= (int) hdt.getDictionary().getNobjects();
        List<Score> scores = new ArrayList<Score>();
        for (int id=1; id<=nShared; id++){
            Score s = new Score();
            s.node = hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT).toString();
            s.pageRank =  pageRankScoresShared[id];
            scores.add(s);
        }
        for (int k=0; k<indexesNonLiterals.size(); k++){
            int id = indexesNonLiterals.get(k);
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
        int nShared = (int) hdt.getDictionary().getNshared();
        int nSubjects = (int) hdt.getDictionary().getNsubjects();
        int nObjects= (int) hdt.getDictionary().getNobjects();
        for (int id=1; id<=nShared; id++){
            System.out.println(hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT) + "\t" + pageRankScoresShared[id]);
        }
        for (int k=0; k<indexesNonLiterals.size(); k++){
            int id = indexesNonLiterals.get(k);
            System.out.println(hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT) + "\t" + pageRankScoresObjects[k]);
        }
        for (int id=1; id<=(nSubjects-nShared); id++){
            System.out.println(hdt.getDictionary().idToString(id+nShared,TripleComponentRole.SUBJECT) + "\t" + startValue);
        }
    }

}
