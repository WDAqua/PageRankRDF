package edu.kit.aifb.model;

import java.io.Serializable;

public class TripleMeta implements Serializable {

	public static enum TripleFocus {subject, object, undefined};
	
	private static final long serialVersionUID = 9052371197747024462L;
	
	URI subject;
	Property predicate;
	URIorLiteral object;
	Double rank;
	TripleFocus focus;
	
	
	public TripleMeta (URI subject, Property predicate, URIorLiteral object, TripleFocus focus) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.focus = focus;
	}
	
	public void setRank(Double rank) {
		this.rank = rank;
	}

	public Double getRank() {
		return rank;
	}
	
	public URI getSubject() {
		return subject;
	}
	
	public Property getPredicate() {
		return predicate;
	}
	
	public URIorLiteral getObject() {
		return object;
	}
	
	public TripleFocus getFocus() {
		return focus;
	}
	
	
	@Override
	public String toString() {
		if (object.isURI()) {
			return "<" + subject.getURI().toString() + ">\t<" + predicate.getURI().toString() + ">\t<" + ((URI) object).getURI().toString() + "> .";	
		} else {
			return "<" + subject.getURI().toString() + ">\t<" + predicate.getURI().toString() + ">\t\"" + ((Literal) object).getValue() + "\" .";
		}
		
		
	}
	
	@Override
	public boolean equals(Object obj) {
		TripleMeta object = (TripleMeta) obj;
		return subject.equals(object.getSubject()) &&
				predicate.equals(object.getPredicate()) &&
				object.equals(object.getObject());
	}
	
	public boolean resourcesMatch(TripleMeta t) {
		if (this.subject.equals(t.getSubject()) && this.object.equals(t.getObject())) {
			return true;
		} else if (((URI) this.object).equals(t.getSubject()) && this.subject.equals((URI) t.getObject())) {
			return true;
		} else {
			return false;
		}
	}
	
}
