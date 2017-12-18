package eu.wdaqua.pagerank;

import java.io.PrintWriter;

public interface PageRank {

    public void compute();

    public void printPageRankScoresTSV(PrintWriter writer);

    public void printPageRankScoresRDF(PrintWriter writer);
}