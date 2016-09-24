package com.restify.http.client.request;

import java.io.OutputStream;
import java.nio.charset.Charset;

import com.restify.http.client.Headers;

public interface HttpRequestMessage {

	public OutputStream output();

	public Headers headers();

	public Charset charset();

}
