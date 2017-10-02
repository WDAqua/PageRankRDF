package edu.kit.aifb.summa.model;

public class URI extends URIorLiteral {

	private static final long serialVersionUID = -2949152516257474547L;
	private java.net.URI uri = null;
	private String label = null;
	
	// language of the label
	private String lang = null;
	
	public URI(java.net.URI uri) {
		this.uri = uri;
		type = Types.URI;
	}
	
	public URI(java.net.URI uri, String label) {
		this.uri = uri;
		this.label = label;
		type = Types.URI;
	}
	
	public URI(java.net.URI uri, String label, String lang) {
		this.uri = uri;
		this.label = label;
		this.setLang(lang);
		type = Types.URI;
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
	public String toString() {
		return "<" + uri.toString() + ">";
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
}
