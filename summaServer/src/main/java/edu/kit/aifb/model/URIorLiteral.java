package edu.kit.aifb.model;

import java.io.Serializable;

public abstract class URIorLiteral implements Serializable {

	private static final long serialVersionUID = -8788934617102071760L;

	public static enum Types {LITERAL, URI};
	protected Types type = null;
	
	public Types getType() {
		return type;
	}
	
	public boolean isLiteral() {
		if (type != null && type.equals(Types.LITERAL)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isURI() {
		if (type != null && type.equals(Types.URI)) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this.type == Types.LITERAL) {
			Literal uriLiteralObject = (Literal) this;
			Literal object = (Literal) obj;
			
			return uriLiteralObject.getValue().equals(object.getValue());
		} else if (this.type == Types.URI) {
			URI uriLiteralObject = (URI) this;
			URI object = (URI) obj;
			
			return uriLiteralObject.getURI().equals(object.getURI());
		} else {
			return false;
		}
	}
}
