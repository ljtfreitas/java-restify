package com.restify.sample.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MyApiResponse {

	@XmlElement
	private String httpMethod;

	@XmlElement
	private String message;

	@Deprecated
	public MyApiResponse() {
	}

	@JsonCreator
	public MyApiResponse(@JsonProperty("httpMethod") String httpMethod, @JsonProperty("message") String message) {
		this.httpMethod = httpMethod;
		this.message = message;
	}

	public String httpMethod() {
		return httpMethod;
	}

	public String message() {
		return message;
	}

	@Override
	public String toString() {
		return "HttpMethod: " + httpMethod + ", Message: " + message;
	}
}
