package com.restify.http.client;

import java.io.InputStream;

public interface HttpResponseMessage {

	public EndpointResponseCode code();

	public Headers headers();

	public InputStream body();
}
