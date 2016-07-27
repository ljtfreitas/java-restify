package com.restify.http.metadata;

public class EndpointMethodParameter {

	public enum EndpointMethodParameterType {
		PATH, HEADER, BODY, QUERY_STRING;
	}

	private final int position;
	private final String name;
	private final EndpointMethodParameterType type;
	private final EndpointMethodParameterSerializer serializer;

	public EndpointMethodParameter(int position, String name) {
		this(position, name, EndpointMethodParameterType.PATH);
	}

	public EndpointMethodParameter(int position, String name, EndpointMethodParameterType type) {
		this(position, name, type, new SimpleEndpointMethodParameterSerializer());
	}

	public EndpointMethodParameter(int position, String name, EndpointMethodParameterType type, EndpointMethodParameterSerializer serializer) {
		this.position = position;
		this.name = name;
		this.type = type;
		this.serializer = serializer;
	}

	public boolean is(String name) {
		return this.name.equals(name);
	}

	public String resolve(Object arg) {
		return serializer.serialize(arg);
	}

	public String name() {
		return name;
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

	public boolean ofQueryString() {
		return type == EndpointMethodParameterType.QUERY_STRING;
	}

}
