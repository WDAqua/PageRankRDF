package edu.kit.aifb.summa;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.openrdf.model.Literal;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;

import edu.kit.aifb.summa.model.Property;
import edu.kit.aifb.summa.model.TripleMeta;
import edu.kit.aifb.summa.model.URI;
import edu.kit.aifb.summa.model.TripleMeta.TripleFocus;
import edu.kit.aifb.summa.servlet.JerseyService;


/**
 * This is an example summarization approach that generates summaries with
 * the DBpedia SPARQL endpoint.
 *
 */
public class SummarizerDBLP implements Summarizer {
	
	private static final String REPOSITORY = "https://wdaqua.univ-st-etienne.fr/hdt-endpoint/dblp/sparql";
	
	private static final String QUERY_0 = "SELECT DISTINCT ?l WHERE { "
			+ "OPTIONAL {<ENTITY> <http://www.w3.org/2000/01/rdf-schema#label> ?l . }}";
			//+ "FILTER regex(lang(?l), \"LANG\", \"i\") . }}";
	
	private static final String QUERY_1 = "PREFIX vrank:<http://purl.org/voc/vrank#> "
			+ "SELECT DISTINCT ?o (SAMPLE(?label) as ?l) "
			//+ "FROM <http://people.aifb.kit.edu/ath/#DBpedia_PageRank> "
			//+ "FROM <http://dbpedia.org> WHERE"
			+ "{<ENTITY> ?p ?o . ?o vrank:hasRank/vrank:rankValue ?pageRank. "
			+ "PREDICATES"
			+ "OPTIONAL {?o <http://www.w3.org/2000/01/rdf-schema#label> ?label . } "
			+ "FILTER (lang(?lable)=\"en\" || lang(?label)=\"\"). "
			+ "}"
			//+ "FILTER regex(lang(?l), \"LANG\", \"i\") .}}"
			+ "GROUP BY ?o ORDER BY DESC (?pageRank) LIMIT TOPK";
	
	private static final String QUERY_2 = "PREFIX vrank:<http://purl.org/voc/vrank#>"
			+ "SELECT ?p ?l ?rank "
			+ "WHERE { "
			//+ "FROM <http://people.aifb.kit.edu/ath/#DBpedia_PageRank> "
			//+ "FROM <http://dbpedia.org> WHERE {"
			+ "<ENTITY> ?p <OBJECT> . "
			+ "<OBJECT> vrank:hasRank/vrank:rankValue ?rank . "
			+ "OPTIONAL {?p <http://www.w3.org/2000/01/rdf-schema#label> ?l. } "
			+ "} ORDER BY asc(?p)";
                        //+ "FILTER regex(lang(?l), \"LANG\", \"i\")} } ORDER BY asc(?p)";
	
	
	/**
	 * main method to test the summarizer
	 */
	public static void main(String[] args) throws URISyntaxException {
		String uri = "http://dblp.l3s.de/d2r/resource/publications/conf/esws/BothDSSC016";

		Summarizer summ = new SummarizerDBLP();
		System.out.println("Start");
		System.out.println(QUERY_0);
		System.out.println(QUERY_1);
		System.out.println(QUERY_2);

		LinkedList<TripleMeta> meta = summ.summarize(new java.net.URI(uri), null, 5, 1, null);
		System.out.println("After");
		for (TripleMeta tripleMeta : meta) {
			System.out.println(tripleMeta.toString());
		}

		//JerseyService service = new JerseyService();
		//service.executeQuery(uri, 5,1,new String[0], "en", RDFFormat.TURTLE);
	}
	
	public LinkedList<TripleMeta> summarize(java.net.URI uri, String[] fixedProperties,
			Integer topK, Integer maxHops, String language) {
		System.out.println("Eneter");
		SPARQLRepository rep = new SPARQLRepository(REPOSITORY);
		System.out.println("Eneter");

		if (language == null) {
			language = "en";
		}
		
		if (fixedProperties == null) {
			fixedProperties = new String [0];
		}
		
		RepositoryConnection con = null;

		 
		LinkedList<TripleMeta> result = new LinkedList<TripleMeta>();
		try {
			rep.initialize();
			con = rep.getConnection();
			System.out.println(QUERY_0.replace("ENTITY", uri.toString()).replace("LANG", language));
			TupleQuery q1 = con.prepareTupleQuery(QueryLanguage.SPARQL, 
					QUERY_0.replace("ENTITY", uri.toString()).replace("LANG", language));
			TupleQueryResult r1 = q1.evaluate();
			URI subject = null;
			System.out.println("query 0"+r1.hasNext());
			if (r1.hasNext()) {
				BindingSet set = r1.next();
				Binding l = set.getBinding("l");
				System.out.println(l.toString());
				if (l == null) {
					subject = new URI(uri);
				} else {
					String langTag = "en";
					if (((Literal) l.getValue()).getLanguage()!=null){
						langTag = ((Literal) l.getValue()).getLanguage();
					}
					subject = new URI(uri, l.getValue().stringValue(), langTag);
				}
				
			}
			r1.close();
			String query1 = QUERY_1.replace("ENTITY", uri.toString()).
					replace("LANG", language).
					replace("TOPK", Integer.toString(topK));
			if (fixedProperties.length > 0) {
				String replacement = "FILTER (";
				for (String string : fixedProperties) {
					replacement += "?p = <" + string + "> || ";
				}
				replacement = replacement.substring(0, replacement.length() - 3);
				replacement += ") .";
				query1 = query1.replaceAll("PREDICATES", replacement);
			} else {
				query1 = query1.replaceAll("PREDICATES", "");
			}
			System.out.println(query1);
			TupleQuery q2 = con.prepareTupleQuery(QueryLanguage.SPARQL, query1);

			TupleQueryResult r2 = q2.evaluate();
			ArrayList<URI> objects = new ArrayList<URI>();
			System.out.println("quer1 "+r2.hasNext());
			while (r2.hasNext()) {
				BindingSet set = r2.next();
				Binding o = set.getBinding("o");
				Binding l = set.getBinding("l");
				URI object = null;
				if (o != null && l == null) {
					object = new URI(new java.net.URI(o.getValue().toString()));
					objects.add(object);
				} else  if (o != null && l != null){
					object = new URI(new java.net.URI(o.getValue().toString()), l.getValue().stringValue(), ((Literal) l.getValue()).getLanguage());
					objects.add(object);
				}
			}
			r2.close();

			for (URI object : objects) {
				TupleQuery q3 = con.prepareTupleQuery(QueryLanguage.SPARQL, 
						QUERY_2.replace("ENTITY", uri.toString()).
						replace("LANG", language).
						replace("OBJECT", object.getURI().toString()));
				TupleQueryResult r3 = q3.evaluate();
				System.out.println(q3);
				if (r3.hasNext()) {
					BindingSet set = r3.next();
					Binding p = set.getBinding("p");
					Binding l = set.getBinding("l");
					Binding rank = set.getBinding("rank");
					
					Property predicate = null;
					if (l == null) {
						predicate = new Property(new java.net.URI(p.getValue().stringValue()));

					} else {
						predicate = new Property(new java.net.URI(p.getValue().stringValue()),
								l.getValue().stringValue());						
					}

					TripleMeta meta = new TripleMeta(subject, predicate, object, TripleFocus.subject);
					meta.setRank(new Double(rank.getValue().stringValue()));
					result.add(meta);
				}
				r3.close();
 			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
