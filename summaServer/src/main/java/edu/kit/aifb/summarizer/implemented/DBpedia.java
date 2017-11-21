package edu.kit.aifb.summarizer.implemented;

import org.springframework.stereotype.Component;

import edu.kit.aifb.summarizer.Summarizer;

/**
 * This is an example summarization approach that generates summaries with
 * the public DBpedia SPARQL endpoint.
 *
 */
@Component
public class DBpedia extends Summarizer {

	public String getName() {
		return "dbpedia";
	}

	public String getRepository(){
		return "http://dbpedia.org/sparql";
	}

	public String getQuery0(){
		return "SELECT DISTINCT ?l FROM <http://dbpedia.org> WHERE { "
				+ "OPTIONAL {<ENTITY> <http://www.w3.org/2000/01/rdf-schema#label> ?l ."
				+ "FILTER regex(lang(?l), \"LANG\", \"i\") . }}";
	}

	public String getQuery1(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#>"
				+ "SELECT DISTINCT ?o ?l ?pageRank"
				+ "FROM <http://people.aifb.kit.edu/ath/#DBpedia_PageRank> "
				+ "FROM <http://dbpedia.org> WHERE"
				+ "{<ENTITY> ?p ?o . ?o vrank:hasRank/vrank:rankValue ?pageRank."
				+ "PREDICATES"
				+ "OPTIONAL {?o <http://www.w3.org/2000/01/rdf-schema#label> ?l . "
				+ "FILTER regex(lang(?l), \"LANG\", \"i\") .}}"
				+ "ORDER BY DESC (?pageRank) LIMIT TOPK";
	}

	public String getQuery1b(){
		return null;
	}

	public String getQuery2() {
		return "PREFIX vrank:<http://purl.org/voc/vrank#>"
				+ "SELECT ?p ?l ?rank "
				+ "FROM <http://people.aifb.kit.edu/ath/#DBpedia_PageRank> "
				+ "FROM <http://dbpedia.org> WHERE {"
				+ "<ENTITY> ?p <OBJECT> . "
				+ "OPTIONAL {?p <http://www.w3.org/2000/01/rdf-schema#label> ?l."
				+ "FILTER regex(lang(?l), \"LANG\", \"i\")} } ORDER BY asc(?p)";
	}

	public String getQuery2b(){
		return null;
	}

}
