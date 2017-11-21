package edu.kit.aifb.summarizer.implemented;

import org.springframework.stereotype.Component;

import edu.kit.aifb.summarizer.Summarizer;

@Component
public class DBLP extends Summarizer {

	public String getName() {
		return "dblp";
	}

	public String getRepository(){
		return "https://wdaqua.univ-st-etienne.fr/hdt-endpoint/dblp/sparql";
	};

	public String getQuery0(){
		return "SELECT DISTINCT ?l WHERE { "
				+ "OPTIONAL {<ENTITY> <http://www.w3.org/2000/01/rdf-schema#label> ?l . }}";
	}

	public String getQuery1(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#> "
				+ "SELECT DISTINCT ?o (SAMPLE(?label) as ?l) (SAMPLE(?rank) as ?pageRank) "
				+ "{<ENTITY> ?p ?o . ?o vrank:hasRank/vrank:rankValue ?rank. "
				+ "PREDICATES"
				+ "OPTIONAL {?o <http://www.w3.org/2000/01/rdf-schema#label> ?label . } "
				+ "FILTER (lang(?lable)=\"en\" || lang(?label)=\"\"). "
				+ "}"
				+ "GROUP BY ?o ORDER BY DESC (?pageRank) LIMIT TOPK";
	}

	public String getQuery1b(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#> "
				+ "SELECT DISTINCT ?o (SAMPLE(?label) as ?l)  (SAMPLE(?rank) as ?pageRank) "
				+ "{?o ?p <ENTITY> . ?o vrank:hasRank/vrank:rankValue ?rank. "
				+ "PREDICATES"
				+ "OPTIONAL {?o <http://www.w3.org/2000/01/rdf-schema#label> ?label . } "
				+ "FILTER (lang(?lable)=\"en\" || lang(?label)=\"\"). "
				+ "}"
				+ "GROUP BY ?o ORDER BY DESC (?pageRank) LIMIT TOPK";
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
