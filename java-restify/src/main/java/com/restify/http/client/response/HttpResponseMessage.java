package com.restify.http.client.response;

import java.io.Closeable;
import java.io.InputStream;

import com.restify.http.client.Headers;

public interface HttpResponseMessage extends Closeable {

	public EndpointResponseCode code();

	public Headers headers();

	public InputStream body();

	public boolean readable();
}
