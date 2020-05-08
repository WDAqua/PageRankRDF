package eu.wdaqua.pagerank;

import eu.wdaqua.pagerank.util.BigDoubleArray;
import eu.wdaqua.pagerank.util.BigIntArray;
import eu.wdaqua.pagerank.util.BinarySearch;

import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;

import org.rdfhdt.hdtjena.NodeDictionary;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PageRankHDT implements PageRank{

    private static double dampingFactor = 0.85D;
    private static double startValue = 0.1D;
    private static int numberOfIterations = 40;
    private HDT hdt;
    private BigIntArray numberOutgoing;
    // we use this one to store the reference and do the swap
    private BigDoubleArray pageRankScoresShared;
    private BigDoubleArray pageRankScoresSharedPrev; 
    private BigDoubleArray pageRankScoresSharedNext; 
    private BigDoubleArray pageRankScoresObjects;
    //used to identify all literals, since the entries in the dictionary are ordered, an interval suffices
    private long start_literals_objects = -1;
    private long end_literals_objects = -1;
    private boolean literals;
    private boolean normalize; 

    public PageRankHDT(String hdtDump){
        this.load(hdtDump);
    }

    public PageRankHDT(String hdtDump, double dampingFactor, double startValue,  int numberOfIterations, boolean literals, boolean normalize){
        this.load(hdtDump);
        this.dampingFactor = dampingFactor;
        this.startValue = startValue;
        this.numberOfIterations = numberOfIterations;
        this.literals = literals;
        this.normalize = normalize; 
    }

    public PageRankHDT(HDT hdt, double dampingFactor, double startValue, int numberOfIterations, boolean literals, boolean normalize){
        this.hdt = hdt;
        this.dampingFactor = dampingFactor;
        this.startValue = startValue;
        this.numberOfIterations = numberOfIterations;
        this.literals = literals;
        this.normalize = normalize; 
    }

    public PageRankHDT(HDT hdt, double dampingFactor, double startValue, int numberOfIterations){
        this.hdt = hdt;
        this.dampingFactor = dampingFactor;
        this.startValue = startValue;
        this.numberOfIterations = numberOfIterations;
        this.literals = false;
        this.normalize = false; 
    }

    void load(String hdtDump){
        try {
            hdt = HDTManager.mapIndexedHDT(hdtDump, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void compute() {
        System.out.println("Computing PageRank: " + numberOfIterations +
                " iterations, damping factor " + dampingFactor +
                ", start value " + startValue +
                ", considering literals " + literals + 
                ", normalizing "+ normalize);

        long nShared = hdt.getDictionary().getNshared();
        long nSubjects = hdt.getDictionary().getNsubjects();
        long nObjects= hdt.getDictionary().getNobjects();

        System.out.println("Initialize the page rank scores");

        //Save indeces which are not literals, don't do pagerank for literal only for not uris
        NodeDictionary nodeDictionary = new NodeDictionary(hdt.getDictionary());
        long numberNonLiterals = 0;
//        System.out.println(nObjects);
//        for (int i = 1; i<=nShared; i++){
//            System.out.println("DICT "+hdt.getDictionary().idToString(i,TripleComponentRole.OBJECT));
//        }
//        System.out.println("-----");
//        for (int i = nShared+1; i<=nObjects; i++){
//            System.out.println("DICT "+hdt.getDictionary().idToString(i,TripleComponentRole.OBJECT));
//        }

        start_literals_objects = -1;
        end_literals_objects = -1;
        if (literals == false){
            start_literals_objects = BinarySearch.first(hdt.getDictionary(), nShared+1, nObjects, "\"");
            end_literals_objects = BinarySearch.last(hdt.getDictionary(), nShared+1, nObjects, nObjects, "\"");
        }
        System.out.println("start_literals_objects "+start_literals_objects);
        System.out.println("end_literals_objects "+end_literals_objects);

        numberNonLiterals = nObjects-(end_literals_objects-start_literals_objects);
        System.out.println("numberNonLiterals "+numberNonLiterals);

        // we only have to store the prev and next values of the shared elements
        // the rest of elemnts are just sinks 
        pageRankScoresSharedPrev = new BigDoubleArray((int)hdt.getDictionary().getNshared()+1);
        pageRankScoresSharedNext = new BigDoubleArray((int)hdt.getDictionary().getNshared()+1); 
        pageRankScoresObjects = new BigDoubleArray(numberNonLiterals+1);
        

        //Initialize the start page rank scores (only the prev set) 
        for (int id=1; id<=nShared; id++){
            pageRankScoresSharedPrev.set(id,startValue);
        }
        for (int k=0; k<numberNonLiterals; k++){
            pageRankScoresObjects.set(k,startValue);
        }

        //Compute the number of outgoing links
        System.out.println("Compute number of outgoing edges");
        numberOutgoing = new BigIntArray(hdt.getDictionary().getNsubjects()+1);
        numberOutgoing.set(0,0);
        for (long id=1; id<=nSubjects; id++) {
            IteratorTripleID iteratorTripleID = hdt.getTriples().search(new TripleID(id, 0, 0));
            int count = 0;
            while (iteratorTripleID.hasNext()){
                long o = iteratorTripleID.next().getObject();
                if (o<start_literals_objects || o > end_literals_objects){
                    count++;
                }
            }
            numberOutgoing.set(id,count);
        }

        //Compute the page rank
        System.out.println("Compute the page rank");
        System.err.println("Iteration...");
        
        // to get the actual number of elements we need 
        // to take into account the "only-subj" elements 
        // numberNonLiterals == (nObjects - literal ones)
        double numberAffectedElements = numberNonLiterals + (hdt.getDictionary().getNsubjects()-hdt.getDictionary().getNshared()); 
        								
        for (int j = 0; j < numberOfIterations; j++) {
            System.err.print(j +" ");
            for (int id=1; id<=nObjects; id++){
                if (!(id>=start_literals_objects && id<=end_literals_objects)) {
                    TripleID root = new TripleID(0, 0, id);
                    IteratorTripleID incomingLinks = hdt.getTriples().search(root);
                    double pageRank = 0.0D; 
                    if (normalize) {
                    	pageRank = (1.0D - dampingFactor) / numberAffectedElements; 
                    }
                    else {
                    	pageRank = (1.0D - dampingFactor); 
                    }
                    while (incomingLinks.hasNext()) {
                        TripleID inLink = incomingLinks.next();
                        Double pageRankIn = 0.0;
                        if (inLink.getSubject() <= nShared) {
                            pageRankIn = pageRankScoresSharedPrev.get(inLink.getSubject());
                        } else {
                        	// the subject is just contributing (not having any incoming contribution) 
                            pageRankIn = startValue;
                        }
                        int numberOut = numberOutgoing.get(inLink.getSubject());
                        pageRank += dampingFactor * (pageRankIn / numberOut);
                    }
                    if (id <= nShared) {
                        pageRankScoresSharedNext.set(id,pageRank);
                    } else {
                        if (id < start_literals_objects){
                            pageRankScoresObjects.set(id,pageRank);
                        } else {
                            pageRankScoresObjects.set(id-(end_literals_objects-start_literals_objects), pageRank);
                        }
                    }
                }
            }
            
            
            // we swap Prev and Next shared storages
            // CBL: I take advantage of the already existing variable to 
            // make the reference swap and reuse the memory
            
            pageRankScoresShared = pageRankScoresSharedNext; 
            pageRankScoresSharedNext = pageRankScoresSharedPrev; 
            pageRankScoresSharedPrev = pageRankScoresShared; 
            
        }
        // in the last iteration pageRankScoresShared == pageRankScoresSharedNext
        // and pageRankScoresObjects are just receiving the influence (so they don't need to have an aux storage). 
        System.out.println("\n");
    }

    public List<PageRankScore> getPageRankScores() {
        long nShared = hdt.getDictionary().getNshared();
        long nSubjects = hdt.getDictionary().getNsubjects();
        long nObjects= hdt.getDictionary().getNobjects();
        List<PageRankScore> scores = new ArrayList<PageRankScore>();
        for (long id=1; id<=nShared; id++){
            PageRankScore s = new PageRankScore();
            s.node = hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT).toString();
            s.pageRank =  pageRankScoresShared.get(id);
            scores.add(s);
        }
        for (long id=nShared+1; id<=nObjects; id++){
            if (id<start_literals_objects){
                PageRankScore s = new PageRankScore();
                s.node = hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT).toString();
                s.pageRank =  pageRankScoresObjects.get(id);
                scores.add(s);
            }
            if (id > end_literals_objects){
                PageRankScore s = new PageRankScore();
                s.node = hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT).toString();
                s.pageRank =  pageRankScoresObjects.get(id-(end_literals_objects-start_literals_objects));
                scores.add(s);
            }
        }
        for (int id=1; id<=(nSubjects-nShared); id++){
            PageRankScore s = new PageRankScore();
            s.node = hdt.getDictionary().idToString(id+nShared,TripleComponentRole.SUBJECT).toString();
            s.pageRank =  startValue;
            scores.add(s);
        }
        return scores;

    }

    public void printPageRankScoresTSV(PrintWriter writer){
        long nShared = hdt.getDictionary().getNshared();
        long nSubjects = hdt.getDictionary().getNsubjects();
        long nObjects= hdt.getDictionary().getNobjects();
        for (long id=1; id<=nShared; id++){
            writer.println(hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT) + "\t" + String.format("%.10f",pageRankScoresShared.get(id)));
        }
        for (long id=nShared+1; id<=nObjects; id++){
            if (id<start_literals_objects){
                writer.println(hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT) + "\t" + String.format("%.10f",pageRankScoresObjects.get(id)));
            }
            if (id > end_literals_objects){
                writer.println(hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT) + "\t" + String.format("%.10f",pageRankScoresObjects.get(id-(end_literals_objects-start_literals_objects))));
            }
        }
        for (long id=1; id<=(nSubjects-nShared); id++){
            writer.println(hdt.getDictionary().idToString(id+nShared,TripleComponentRole.SUBJECT) + "\t" + String.format("%.10f",startValue));
        }
    }

    public void printPageRankScoresRDF(PrintWriter writer){
        long nShared = hdt.getDictionary().getNshared();
        long nSubjects = hdt.getDictionary().getNsubjects();
        long nObjects= hdt.getDictionary().getNobjects();
        for (long id=1; id<=nShared; id++){
            writer.println("<"+hdt.getDictionary().idToString(id,TripleComponentRole.SUBJECT)+"> <http://purl.org/voc/vrank#pagerank>\t \""+String.format("%.10f", pageRankScoresShared.get(id))+"\"^^<http://www.w3.org/2001/XMLSchema#float> .");
        }
        for (long id=nShared+1; id<=nObjects; id++){
            if (id<start_literals_objects){
                writer.println("<"+hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT)+"> <http://purl.org/voc/vrank#pagerank>\t \""+String.format("%.10f",pageRankScoresObjects.get(id))+"\"^^<http://www.w3.org/2001/XMLSchema#float> .");
            }
            if (id > end_literals_objects){
                writer.println("<"+hdt.getDictionary().idToString(id,TripleComponentRole.OBJECT)+"> <http://purl.org/voc/vrank#pagerank>\t \""+String.format("%.10f",pageRankScoresObjects.get(id-(end_literals_objects-start_literals_objects)))+"\"^^<http://www.w3.org/2001/XMLSchema#float> .");
            }
        }
        for (long id=1; id<=(nSubjects-nShared); id++){
            writer.println("<"+hdt.getDictionary().idToString(id+nShared,TripleComponentRole.SUBJECT)+"> <http://purl.org/voc/vrank#pagerank>\t \""+String.format("%.10f",startValue)+"\"^^<http://www.w3.org/2001/XMLSchema#float> .");
        }
    }
}
