package eu.wdaqua.pagerank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageRank
{
    private static final String NT_PATTERN = "^(?:<([^>]+)>\\s*){3}\\s*.";
    private static final String NQ_PATTERN = "^(?:<([^>]+)>\\s*){4}\\s*.";
    private static final String HELP_MESSAGE = "Name\tRDF eu.wdaqua.pagerank.PageRank\nParameters\n\t-D\tdamping factor, double (default 0.85); example -D0.5\n\t-I\tnumber of iterations, integer (default 40); example -I20\n\t-S\tstart value, double (default 0.1); example -S1.0\nSupported file formats: nt (N-Triples), nq (N-Quads).\nNote: This program only uses the (directed) link structure of an RDF graph (It ignores predicates and graph names. Duplicate A->B links will be counted twice.)\nExample calls for this program:\n\tMinimum example: java -jar pagerank.jar file.nt\n\tMaximum example: java -jar -Xmx200G -Dfile.encoding=\"UTF-8\" pagerank.jar -D0.85 -I35 -S0.5 file.nt > result.tsv\n\n(file names that contain 'help' will get you here)\n\nAuthor: Andreas Thalhammer (http://andreas.thalhammer.bayern)\nVersion: 1.0";
    private static double dampingFactor = 0.85D;
    private static double startValue = 0.1D;
    private static int numberOfIterations = 40;

    public static void main(String[] args)
    {
        Pattern ntPattern = Pattern.compile("^(?:<([^>]+)>\\s*){3}\\s*.");
        Pattern nqPattern = Pattern.compile("^(?:<([^>]+)>\\s*){4}\\s*.");

        String filename = "";
        String[] arrayOfString1 = args;int j = args.length;
        for (int i = 0; i < j; i++)
        {
            String arg = arrayOfString1[i];
            if (arg.startsWith("-I"))
            {
                try
                {
                    numberOfIterations = Integer.parseInt(arg.substring(2));
                }
                catch (NumberFormatException e)
                {
                    System.err.println("Couldn't process parameter -I\n... Non-standard integer value '" +
                            arg.substring(2) +
                            "' (see https://docs.oracle.com/javase/8/docs/api/java/lang/Integer.html).");
                    System.exit(-1);
                    if (numberOfIterations > 0) {
                        continue;
                    }
                }
                System.err.println("Unsupported number of iterations '" + numberOfIterations + "'. Sould be > 0.");
                System.exit(-1);
            }
            else if (arg.startsWith("-S"))
            {
                try
                {
                    startValue = Double.parseDouble(arg.substring(2));
                }
                catch (NumberFormatException e)
                {
                    System.err.println("Couldn't process parameter -S\n... Non-standard double value '" +
                            arg.substring(2) +
                            "' (see https://docs.oracle.com/javase/8/docs/api/java/lang/Double.html).");
                    System.exit(-1);
                }
                if (startValue <= 0.0D)
                {
                    System.err.println("Unsupported start value '" + startValue + "'. Sould be > 0.");
                    System.exit(-1);
                }
            }
            else if (arg.startsWith("-D"))
            {
                try
                {
                    dampingFactor = Double.parseDouble(arg.substring(2));
                }
                catch (NumberFormatException e)
                {
                    System.err.println("Couldn't process parameter -D\n... Non-standard double value '" +
                            arg.substring(2) +
                            "' (see https://docs.oracle.com/javase/8/docs/api/java/lang/Double.html).");
                    System.exit(-1);
                }
                if ((dampingFactor > 1.0D) || (dampingFactor < 0.0D))
                {
                    System.err.println("Unsupported damping factor '" + dampingFactor + "'. Sould be >= 0 && <= 1.");
                    System.exit(-1);
                }
            }
            else if ((arg.contains("help")) || (arg.toLowerCase().matches("(-)+h$")))
            {
                System.out.println("Name\tRDF eu.wdaqua.pagerank.PageRank\nParameters\n\t-D\tdamping factor, double (default 0.85); example -D0.5\n\t-I\tnumber of iterations, integer (default 40); example -I20\n\t-S\tstart value, double (default 0.1); example -S1.0\nSupported file formats: nt (N-Triples), nq (N-Quads).\nNote: This program only uses the (directed) link structure of an RDF graph (It ignores predicates and graph names. Duplicate A->B links will be counted twice.)\nExample calls for this program:\n\tMinimum example: java -jar pagerank.jar file.nt\n\tMaximum example: java -jar -Xmx200G -Dfile.encoding=\"UTF-8\" pagerank.jar -D0.85 -I35 -S0.5 file.nt > result.tsv\n\n(file names that contain 'help' will get you here)\n\nAuthor: Andreas Thalhammer (http://andreas.thalhammer.bayern)\nVersion: 1.0");
                System.exit(-1);
            }
            else
            {
                filename = arg;
            }
        }
        if (filename.equals(""))
        {
            System.err.println("No input file provided. Please use '-H' for more information.");
            System.exit(-1);
        }
        HashMap<String, Integer> numberOutgoing = new HashMap();
        Object incomingPerPage = new HashMap();
        long time = System.currentTimeMillis();
        try
        {
            File f = new File(filename);
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String currentLine = reader.readLine();
            while (currentLine != null)
            {
                Matcher ntMatcher = ntPattern.matcher(currentLine);
                Matcher nqMatcher = nqPattern.matcher(currentLine);
                if ((ntMatcher.matches()) || (nqMatcher.matches()))
                {
                    String[] split = currentLine.split(">\\s*");

                    String out = split[0].substring(1);
                    String in = split[2].substring(1);

                    ArrayList<String> incoming = (ArrayList)((HashMap)incomingPerPage).get(in);
                    if (incoming == null)
                    {
                        incoming = new ArrayList();
                        ((HashMap)incomingPerPage).put(in, incoming);
                    }
                    ArrayList<String> incoming2 = (ArrayList)((HashMap)incomingPerPage).get(out);
                    if (incoming2 == null) {
                        ((HashMap)incomingPerPage).put(out, new ArrayList());
                    }
                    incoming.add(out);
                    Integer numberOut = (Integer)numberOutgoing.get(out);
                    if (numberOut == null) {
                        numberOutgoing.put(out, Integer.valueOf(1));
                    } else {
                        numberOutgoing.put(out, Integer.valueOf(numberOut.intValue() + 1));
                    }
                }
                currentLine = reader.readLine();
            }
            reader.close();
        }
        catch (IOException e)
        {
            System.err.println("Couldn't process file named " + filename);
            System.exit(-1);
        }
        time = System.currentTimeMillis() - time;
        System.err.println("Reading input took " + time / 1000L + "s");
        time = System.currentTimeMillis();
        compute(numberOutgoing, (HashMap)incomingPerPage);
        time = System.currentTimeMillis() - time;
        System.err.println("Computing eu.wdaqua.pagerank.PageRank took " + time / 1000L + "s");
    }

    public static void compute(HashMap<String, Integer> numberOutgoing, HashMap<String, ArrayList<String>> incomingPerPage)
    {
        System.err.println("Computing eu.wdaqua.pagerank.PageRank: " + numberOfIterations +
                " iterations, damping factor " + dampingFactor +
                ", start value " + startValue);
        HashMap<String, Double> pageRankScores = new HashMap();
        Set<String> keyset = incomingPerPage.keySet();

        System.err.println("Iteration...");
        for (int j = 1; j <= numberOfIterations; j++)
        {
            System.err.print(j);
            for (String string : keyset)
            {
                ArrayList<String> incomingLinks = (ArrayList)incomingPerPage.get(string);

                double pageRank = 1.0D - dampingFactor;
                for (String inLink : incomingLinks)
                {
                    Double pageRankIn = (Double)pageRankScores.get(inLink);
                    if (pageRankIn == null) {
                        pageRankIn = Double.valueOf(startValue);
                    }
                    int numberOut = ((Integer)numberOutgoing.get(inLink)).intValue();
                    pageRank += dampingFactor * (pageRankIn.doubleValue() / numberOut);
                }
                pageRankScores.put(string, Double.valueOf(pageRank));
            }
            System.err.print('.');
        }
        System.err.println();

        Set<String> keysetNew = pageRankScores.keySet();
        for (String string : keysetNew) {
            System.out.println(string + "\t" + pageRankScores.get(string));
        }
    }
}
