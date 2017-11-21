package edu.kit.aifb.summarizer.implemented;

import org.springframework.stereotype.Component;

import edu.kit.aifb.summarizer.Summarizer;

/**
 * This is an example summarization approach that generates summaries with
 * the public DBpedia SPARQL endpoint.
 *
 */
@Component
public class Freebase extends Summarizer {

	public String getName() {
		return "freebase";
	}

	public String getRepository(){
		return "https://wdaqua-hdt-endpoint.univ-st-etienne.fr/freebase_big/sparql";
	}

	public String getQuery0(){
		return "SELECT DISTINCT ?l WHERE { "
				+ "OPTIONAL {<ENTITY> <http://www.w3.org/2000/01/rdf-schema#label> ?l . "
				+ "FILTER regex(lang(?l), \"LANG\", \"i\") . }}";
	}

	public String getQuery1(){
		return "PREFIX vrank:<http://purl.org/voc/vrank#> " +
				"SELECT DISTINCT ?o ?l ?pageRank " +
				"WHERE { " +
				"<ENTITY> ?p ?o . " +
				"PREDICATES" +
				"FILTER (?p != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> && " +
				"?p != <http://rdf.freebase.com/ns/type.object.type> && " +
				"?p != <http://rdf.freebase.com/ns/common.topic.webpage..common.webpage.in_index> && " +
				"?p != <http://rdf.freebase.com/ns/common.topic.webpage..common.webpage.category> && " +
				"?p != <http://rdf.freebase.com/ns/base.ontologies.ontology_instance.equivalent_instances..base.ontologies.ontology_instance_mapping.ontology>) " +
				"?o <http://www.w3.org/2000/01/rdf-schema#label> ?l . " +
				"FILTER STRENDS(lang(?l), \"LANG\") . " +
				"graph <http://freebase.com/pageRank> { " +
				"?o <http://purl.org/voc/vrank#pagerank> ?pageRank . " +
				"}} " +
				"ORDER BY DESC (?pageRank) LIMIT TOPK ";
	}

	public String getQuery1b(){
		return null;
	}

	public String getQuery2() {
		return "PREFIX vrank:<http://purl.org/voc/vrank#>"
				+ "SELECT ?p ?l "
				+ "WHERE {"
				+ "<ENTITY> ?p <OBJECT> ."
				+ "OPTIONAL {?p <http://www.w3.org/2000/01/rdf-schema#label> ?l . "
				+ "FILTER regex(lang(?l), \"LANG\", \"i\")} } ORDER BY asc(?p) ";
	}

	public String getQuery2b(){
		return null;
	}

}
