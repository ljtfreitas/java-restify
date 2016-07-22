package com.restify.http.metadata;

public class EndpointMethodParameter {

	public enum EndpointMethodParameterType {
		PATH, HEADER, BODY;
	}

	private final int position;
	private final String name;
	private final EndpointMethodParameterType type;

	public EndpointMethodParameter(int position, String name) {
		this(position, name, EndpointMethodParameterType.PATH);
	}

	public EndpointMethodParameter(int position, String name, EndpointMethodParameterType type) {
		this.position = position;
		this.name = name;
		this.type = type;
	}

	public boolean is(String name) {
		return this.name.equals(name);
	}

	public String resolve(Object arg) {
		return arg.toString();
	}

	public int position() {
		return position;
	}

	public boolean ofBody() {
		return type == EndpointMethodParameterType.BODY;
	}

	public boolean ofPath() {
		return type == EndpointMethodParameterType.PATH;
	}

	public boolean ofHeader() {
		return type == EndpointMethodParameterType.HEADER;
	}

}
