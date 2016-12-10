/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.netflix.client.request.ribbon;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.netflix.client.ClientException;
import com.netflix.client.IResponse;

class RibbonResponse implements IResponse {

	private final HttpResponseMessage source;

	public RibbonResponse(HttpResponseMessage source) {
		this.source = source;
	}

	@Override
	public void close() throws IOException {
		source.close();
	}

	@Override
	public Object getPayload() throws ClientException {
		return null;
	}

	@Override
	public boolean hasPayload() {
		return source.isReadable();
	}

	@Override
	public boolean isSuccess() {
		return source.code().isSucess();
	}

	@Override
	public URI getRequestedURI() {
		return null;
	}

	@Override
	public Map<String, ?> getHeaders() {
		return source.headers().all().stream()
				.collect(Collectors.groupingBy(Header::name));
	}

	public HttpResponseMessage unwrap() {
		return source;
	}
}
