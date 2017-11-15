package edu.kit.aifb.model;


public class Literal extends URIorLiteral {

	private static final long serialVersionUID = 91092509667169783L;
	String value;
	
	public Literal(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
