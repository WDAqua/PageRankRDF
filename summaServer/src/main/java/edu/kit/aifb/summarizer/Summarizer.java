package edu.kit.aifb.summarizer;

import org.openrdf.model.Literal;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

import java.util.ArrayList;
import java.util.LinkedList;

import edu.kit.aifb.model.Property;
import edu.kit.aifb.model.TripleMeta;
import edu.kit.aifb.model.URI;


/**
 * This abstract class needs to be implemented by each entity summarization system.
 *
 */
public abstract class Summarizer {

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

	// method returns a SPARQL query for retriving the label of the predicate
	public abstract String getQuery2();

	public LinkedList<TripleMeta> summarize(java.net.URI uri, String[] fixedProperties,
											Integer topK, Integer maxHops, String language){
		SPARQLRepository rep = new SPARQLRepository(this.getRepository());

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
			TupleQuery q1 = con.prepareTupleQuery(QueryLanguage.SPARQL, this.getQuery0().replace("ENTITY", uri.toString()).replace("LANG", language));
			TupleQueryResult r1 = q1.evaluate();
			URI subject = null;
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
			String query1 = this.getQuery1().replace("ENTITY", uri.toString()).
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
			System.out.println("Query 1"+query1);
			TupleQuery q2 = con.prepareTupleQuery(QueryLanguage.SPARQL, query1);

			TupleQueryResult r2 = q2.evaluate();
			ArrayList<URI> objects = new ArrayList<URI>();
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
						this.getQuery2().replace("ENTITY", uri.toString()).
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

					TripleMeta meta = new TripleMeta(subject, predicate, object, TripleMeta.TripleFocus.subject);
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
