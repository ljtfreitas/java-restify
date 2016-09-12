package com.restify.http.client;

import java.io.OutputStream;
import java.nio.charset.Charset;

public interface HttpRequestMessage {

	public OutputStream output();

	public Headers headers();

	public Charset charset();

}
