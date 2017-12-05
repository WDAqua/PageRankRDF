package edu.kit.aifb.summarizer;

import org.openrdf.model.Literal;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;

import edu.kit.aifb.WebController;
import edu.kit.aifb.model.Property;
import edu.kit.aifb.model.TripleMeta;
import edu.kit.aifb.model.URI;


/**
 * This abstract class needs to be implemented by each entity summarization system.
 *
 */
public abstract class Summarizer {

	private static final Logger logger = LoggerFactory.getLogger(Summarizer.class);
	public abstract String getName();

	// method that returns the SPARQL endpoint of the corresponding knowledgebase
	public abstract String getRepository();

	// method that returns SPARQL query for retriving the label of an entity. The SPARQL query
	// contains the placeholder ENTITY for the uri of the entity and LANG for the language
	public abstract String getQuery0();

	// method returns a SPARQL query for retriving for an entity the objects with highest page
	// rank score together with the label. The SPARQL query contains the placeholder ENTITY for
	// the uri of the entity, LANG for the language of the object labels, TOPK indicating the
	// number of objects to consider
	public abstract String getQuery1();

	public abstract String getQuery1b();

	// method returns a SPARQL query for retriving the label of the predicate
	public abstract String getQuery2();

	public abstract String getQuery2b();

	private URI getLableEntity(RepositoryConnection con, java.net.URI uri, String language){
		System.out.println(this.getQuery0());
		System.out.println(uri.toString());
		System.out.println(language.toString());
		String query = this.getQuery0().replace("ENTITY", uri.toString()).replace("LANG", language);
		logger.info("Query to find the label of the subject {}",query);
		URI subject = null;
		try {
			TupleQuery q1 = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			logger.info("Query to find the predicate {}",q1.toString());
			TupleQueryResult r1 = null;
			r1 = q1.evaluate();
			if (r1.hasNext()) {
                BindingSet set = r1.next();
                Binding l = set.getBinding("l");
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
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		}
		return subject;
	}

	private ArrayList<ObjectRank> getObjects(RepositoryConnection con, java.net.URI uri, String query, String language, int topK, String[] fixedProperties){
		ArrayList<ObjectRank> objects = new ArrayList<ObjectRank>();
		query = query.replace("ENTITY", uri.toString()).
				replace("LANG", language).
				replace("TOPK", Integer.toString(topK));
		logger.info("Query to find the top k objects {}",query.toString());
		try {
			if (fixedProperties.length > 0) {
				String replacement = "FILTER (";
				for (String string : fixedProperties) {
					replacement += "?p = <" + string + "> || ";
				}
				replacement = replacement.substring(0, replacement.length() - 3);
				replacement += ") .";
				query = query.replaceAll("PREDICATES", replacement);
			} else {
				query = query.replaceAll("PREDICATES", "");
			}
			TupleQuery q2 = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			TupleQueryResult r2 = q2.evaluate();

			while (r2.hasNext()) {
				BindingSet set = r2.next();
				Binding o = set.getBinding("o");
				Binding l = set.getBinding("l");
				Binding rank = set.getBinding("pageRank");
				ObjectRank objectRank = null;
				if (o != null && l == null) {
					objectRank = new ObjectRank( new URI(new java.net.URI(o.getValue().toString())), new Double(rank.getValue().stringValue()));
				} else  if (o != null && l != null){
					URI tmp = new URI(new java.net.URI(o.getValue().toString()), l.getValue().stringValue(), ((Literal) l.getValue()).getLanguage());
					objectRank = new ObjectRank( tmp, new Double(rank.getValue().stringValue()));
				}
				if (objectRank!=null){
					objects.add(objectRank);
				}

			}
			r2.close();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return objects;
	}

	private Property getPredicate(RepositoryConnection con, java.net.URI uri, String query, String language, ObjectRank object) {
		query = query.replace("ENTITY", uri.toString()).
				replace("LANG", language).
				replace("OBJECT", object.uri.getURI().toString());
		logger.info("Query to find the predicate {}",query);
		Property predicate = null;
		TupleQuery q3 = null;
		try {

			q3 = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			logger.info("Query to find the predicate {}",query);
			TupleQueryResult r3 = q3.evaluate();
			if (r3.hasNext()) {
				BindingSet set = r3.next();
				Binding p = set.getBinding("p");
				Binding l = set.getBinding("l");
				if (l == null) {
					if (this.getName().equals("freebase")) { //hack to make it work over freebase where there are no labels for the properties, bad usage of RDF standards
						String label = p.getValue().stringValue();
						label = label.substring(label.lastIndexOf(".") + 1, label.length());
						predicate = new Property(new java.net.URI(p.getValue().stringValue()), label);

					} else {
						predicate = new Property(new java.net.URI(p.getValue().stringValue()));
					}


				} else {
					predicate = new Property(new java.net.URI(p.getValue().stringValue()),
							l.getValue().stringValue());
				}
			}
			//r3.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		} catch (QueryEvaluationException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		return predicate;
	}

	public LinkedList<TripleMeta> summarize(java.net.URI uri, String[] fixedProperties,
											Integer topK, Integer maxHops, String language){
		SPARQLRepository rep = new SPARQLRepository(this.getRepository());

		if (fixedProperties == null) {
			fixedProperties = new String [0];
		}

		RepositoryConnection con = null;

		LinkedList<TripleMeta> result = new LinkedList<TripleMeta>();
		try {
			rep.initialize();
			con = rep.getConnection();
			//Retriving the label of uri
			URI subject = getLableEntity(con,uri,language);

			//Retrive the TopK objects, i.e. the objects with k-th top page rank
			//Retrive ouitgoing links
			ArrayList<ObjectRank> objects = new ArrayList<ObjectRank>();
			objects = getObjects(con, uri, this.getQuery1(), language, topK, fixedProperties);
			ArrayList<ObjectRank> objects_rev = new ArrayList<ObjectRank>();
			//If needed (and possible) add ingoing links
			if (objects.size()<topK && this.getQuery1b()!=null){ // go here only if not enough entities have been found
				objects_rev = getObjects(con, uri, this.getQuery1b(), language, topK, fixedProperties);
			}

			//Retrive for the TopK objects, the properties connecting them to the objects together with the label
			for (ObjectRank object : objects) {
				Property predicate = getPredicate(con, uri, this.getQuery2(), language, object);
				//Add the found informations to the model
				TripleMeta meta = new TripleMeta(subject, predicate, object.uri, TripleMeta.TripleFocus.subject);
				meta.setRank(new Double(object.rank));
				result.add(meta);
			}
			for (ObjectRank object : objects_rev) {
				Property predicate = getPredicate(con, uri, this.getQuery2b(),language, object);
				//Add the found informations to the model
				TripleMeta meta = new TripleMeta(subject, predicate, object.uri, TripleMeta.TripleFocus.subject);
				meta.setRank(new Double(object.rank));
				result.add(meta);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	class ObjectRank {
		public URI uri;
		public double rank;

		public ObjectRank(URI uri, double rank){
			this.uri = uri;
			this.rank = rank;
		}
	}
}
