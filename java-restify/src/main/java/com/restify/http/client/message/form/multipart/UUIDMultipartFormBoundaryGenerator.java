package com.restify.http.client.message.form.multipart;

import java.util.UUID;

class UUIDMultipartFormBoundaryGenerator implements MultipartFormBoundaryGenerator {

	@Override
	public String generate() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

}
