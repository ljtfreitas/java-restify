package com.restify.http.client.converter.form.multipart;

class SimpleMultipartFormBoundaryGenerator implements MultipartFormBoundaryGenerator {

	private final String boundary;

	SimpleMultipartFormBoundaryGenerator(String boundary) {
		this.boundary = boundary;
	}

	@Override
	public String generate() {
		return boundary;
	}
}