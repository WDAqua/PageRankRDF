package edu.kit.aifb.summa.summarizer;

public class SummarizerMusicBrainz extends Summarizer {

	public String getRepository(){
		return "https://wdaqua.univ-st-etienne.fr/hdt-endpoint/musicbrainz/sparql";
	}

	public String getQuery0(){
		return "SELECT DISTINCT ?l WHERE { "
				+ "OPTIONAL {<ENTITY> <http://www.w3.org/2000/01/rdf-schema#label> ?l . }"
				+ "OPTIONAL {<ENTITY> <http://xmlns.com/foaf/0.1/name> ?l } "
				+ "OPTIONAL {<ENTITY> <http://purl.org/dc/elements/1.1/title> ?l }}";
	}

	public String getQuery1(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#> "
				+ "SELECT DISTINCT ?o (SAMPLE(?label) as ?l) "
				+ "{<ENTITY> ?p ?o . ?o vrank:hasRank/vrank:rankValue ?pageRank. "
				+ "PREDICATES"
				+ "OPTIONAL {?o <http://www.w3.org/2000/01/rdf-schema#label> ?label . } "
				+ "OPTIONAL {?o <http://xmlns.com/foaf/0.1/name> ?label } "
				+ "OPTIONAL {?o <http://purl.org/dc/elements/1.1/title> ?label } "
				+ "FILTER (lang(?lable)=\"en\" || lang(?label)=\"\"). "
				+ "}"
				+ "GROUP BY ?o ORDER BY DESC (?pageRank) LIMIT TOPK";
	}

	public String getQuery2() {
		return "PREFIX vrank:<http://purl.org/voc/vrank#>"
				+ "SELECT ?p ?l ?rank "
				+ "WHERE { "
				+ "<ENTITY> ?p <OBJECT> . "
				+ "<OBJECT> vrank:hasRank/vrank:rankValue ?rank . "
				+ "OPTIONAL {?p <http://www.w3.org/2000/01/rdf-schema#label> ?l. } "
				+ "} ORDER BY asc(?p)";
	}
}
