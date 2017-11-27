package edu.kit.aifb.summarizer.implemented;

import org.springframework.stereotype.Component;

import edu.kit.aifb.summarizer.Summarizer;

@Component
public class Scigraph extends Summarizer {

	public String getName() {
		return "scigraph";
	}

	public String getRepository(){
		return "https://wdaqua.univ-st-etienne.fr/hdt-endpoint/scigraph/sparql";
	};

	public String getQuery0(){
		return "SELECT DISTINCT ?l WHERE { "
				+ "OPTIONAL {<ENTITY> <http://www.w3.org/2004/02/skos/core#altLabel> ?l . }"
				+ "OPTIONAL {<ENTITY> <http://scigraph.springernature.com/ontologies/core/publishedName> ?l } "
				+ "OPTIONAL {<ENTITY> <http://scigraph.springernature.com/ontologies/core/title> ?l }}";
	}

	public String getQuery1(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#> " +
				"SELECT DISTINCT ?o ?l ?pageRank " +
				"WHERE { " +
				"<ENTITY> ?p ?o . " +
				"PREDICATES " +
				"OPTIONAL {?o <http://www.w3.org/2004/02/skos/core#altLabel> ?l . }" +
				"OPTIONAL {?o <http://scigraph.springernature.com/ontologies/core/publishedName> ?l } " +
				"OPTIONAL {?o <http://scigraph.springernature.com/ontologies/core/title> ?l } " +
				"graph <http://scigraph.com/pageRank> { " +
				"?o <http://purl.org/voc/vrank#pagerank> ?pageRank . " +
				"}} " +
				"ORDER BY DESC (?pageRank) LIMIT TOPK ";
	}

	public String getQuery1b(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#> " +
				"SELECT DISTINCT ?o ?l ?pageRank " +
				"WHERE { " +
				"?o ?p <ENTITY> . " +
				"PREDICATES " +
				"OPTIONAL {?o <http://www.w3.org/2004/02/skos/core#altLabel> ?l . }" +
				"OPTIONAL {?o <http://scigraph.springernature.com/ontologies/core/publishedName> ?l } " +
				"OPTIONAL {?o <http://scigraph.springernature.com/ontologies/core/title> ?l }} " +
				"graph <http://scigraph.com/pageRank> { " +
				"?o <http://purl.org/voc/vrank#pagerank> ?pageRank . " +
				"}} " +
				"ORDER BY DESC (?pageRank) LIMIT TOPK ";
	}

	public String getQuery2(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#>"
				+ "SELECT ?p ?l "
				+ "WHERE { "
				+ "<ENTITY> ?p <OBJECT> . "
				+ "OPTIONAL {?p <http://www.w3.org/2000/01/rdf-schema#label> ?l . "
				+ "FILTER regex(lang(?l), \"LANG\", \"i\")} "
				+ "} ORDER BY asc(?p)";
	}

	public String getQuery2b(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#>"
				+ "SELECT ?p ?l "
				+ "WHERE { "
				+ "<OBJECT> ?p <ENTITY> . "
				+ "OPTIONAL {?p <http://www.w3.org/2000/01/rdf-schema#label> ?l . "
				+ "FILTER regex(lang(?l), \"LANG\", \"i\")} "
				+ "} ORDER BY asc(?p)";
	}
}
