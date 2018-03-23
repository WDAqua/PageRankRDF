package eu.wdaqua;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.validators.PositiveInteger;

import eu.wdaqua.pagerank.PageRank;
import eu.wdaqua.pagerank.PageRankHDT;
import eu.wdaqua.pagerank.PageRankRDF;
import eu.wdaqua.validation.AllowedFormats;
import eu.wdaqua.validation.FileCanBeOpenedValidator;
import eu.wdaqua.validation.FileExistValidator;
import eu.wdaqua.validation.PositiveDouble;
import eu.wdaqua.validation.ZeroOneDouble;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by Dennis on 15/11/17.
 */
class Main {
    @Parameter(names={"--iteration", "-I"},validateWith = PositiveInteger.class, description = "specifying the number are performed by PageRank")
    private int numberOfIterations = 40;
    @Parameter(names={"--start-value", "-S"}, validateWith = PositiveDouble.class,  description = "specifying the start value for the PageRank computation")
    private Double startValue = 0.1;
    @Parameter(names={"--dumping", "-D"}, validateWith = ZeroOneDouble.class, description = "specifying the dumping factor for the PageRank computation")
    private Double dampingFactor = 0.85;
    @Parameter(names={"--string"}, description = "option to compute the page rank for strings or not")
    private Boolean string = false;
    @Parameter(names = "--help", help = true, description = "displays the list of possible parameters")
    private boolean help = false;
    @Parameter(names={"--input", "-in"}, required = true, validateWith = FileExistValidator.class, description = "specify a file in some RDF format or in HDT")
    private String input;
    @Parameter(names={"--output", "-out"}, required = true, validateWith = FileCanBeOpenedValidator.class, description = "specify the file where the pagerank scores are stored")
    private String output;
    @Parameter(names={"--format", "-f"}, validateWith = AllowedFormats.class, description = "specify the output format for the PageRank scores, either \"tsv\" or \"nt\"")
    private String outputFormat = "nt";

    public static void main(String ... argv) {
        Main main = new Main();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(main)
                .build();
        jCommander.setProgramName("java -jar pagerank.jar -in file -out pagerank");
        try {
            jCommander.parse(argv);
            main.run();
        } catch (ParameterException e) {
            e.printStackTrace();
            System.out.println("This is a command line tool for PageRank computation over RDF graphs");
            System.out.println("Example usage: java -jar pagerank.jar -in file -out pagerank");
            System.out.println();
            e.usage();
            System.out.println();
            System.out.println("Note: This program only uses the (directed) link structure of an RDF graph (It ignores predicates and graph names. Duplicate A->B links will be counted twice.)");
            System.out.println("Authors: Andreas Thalhammer (http://andreas.thalhammer.bayern) && Dennis Diefenbach (dennis.diefenbach@univ-st-etienne.fr) ");
            System.out.println("Version: 1.0");
            System.exit(1);
        }
    }

    public void run() {
        try {
            PageRank pr = null;
            long startTime = System.nanoTime();
            if (input.endsWith(".hdt")){
                pr = new PageRankHDT(input, dampingFactor, startValue, numberOfIterations, string);
                pr.compute();
            } else {
                pr = new PageRankRDF(input, dampingFactor, startValue, numberOfIterations, string);
                pr.compute();
            }
            PrintWriter writer = new PrintWriter(output, "UTF-8");
            if (outputFormat=="nt"){
                pr.printPageRankScoresRDF(writer);
            } else {
                pr.printPageRankScoresTSV(writer);
            }
            writer.close();
            long estimatedTime = System.nanoTime() - startTime;
            System.out.println("The computation took "+estimatedTime);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }


}

