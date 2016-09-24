package com.restify.http.contract.metadata;

import java.lang.reflect.Type;

public class EndpointMethodParameter {

	public enum EndpointMethodParameterType {
		PATH, HEADER, BODY, QUERY_STRING;
	}

	private final int position;
	private final String name;
	private final Type javaType;
	private final EndpointMethodParameterType type;
	private final EndpointMethodParameterSerializer serializer;

	public EndpointMethodParameter(int position, String name, Type javaType) {
		this(position, name, javaType, EndpointMethodParameterType.PATH);
	}

	public EndpointMethodParameter(int position, String name, Type javaType, EndpointMethodParameterType type) {
		this(position, name, javaType, type, new SimpleEndpointMethodParameterSerializer());
	}

	public EndpointMethodParameter(int position, String name, Type javaType, EndpointMethodParameterType type,
			EndpointMethodParameterSerializer serializer) {
		this.position = position;
		this.name = name;
		this.javaType = javaType;
		this.type = type;
		this.serializer = serializer;
	}

	public boolean is(String name) {
		return this.name.equals(name);
	}

	public String resolve(Object arg) {
		return serializer.serialize(name, javaType, arg);
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

	public boolean ofQuery() {
		return type == EndpointMethodParameterType.QUERY_STRING;
	}
}
