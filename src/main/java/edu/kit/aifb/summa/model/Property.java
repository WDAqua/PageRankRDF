package edu.kit.aifb.summa.model;

import java.io.Serializable;

public class Property implements Serializable {
	

	private static final long serialVersionUID = 8076694223982669737L;

	private java.net.URI uri = null;
	private String label = null;
	
	public Property(java.net.URI uri) {
		this.uri = uri;
	}
	
	public Property(java.net.URI uri, String label) {
		this.uri = uri;
		this.label = label;
	}
	
	public java.net.URI getURI() {
		return uri;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public boolean equals(Object obj) {
		Property object = (Property) obj;
		return getURI().equals(object.getURI());
	}
	
	@Override
	public String toString() {
		return "<" + uri.toString() + ">";
	}
}
