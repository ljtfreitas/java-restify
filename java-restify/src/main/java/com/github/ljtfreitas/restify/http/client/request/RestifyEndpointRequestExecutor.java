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
package com.github.ljtfreitas.restify.http.client.request;

import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

public class RestifyEndpointRequestExecutor implements EndpointRequestExecutor {

	private final HttpClientRequestFactory httpClientRequestFactory;
	private final EndpointRequestWriter endpointRequestWriter;
	private final EndpointResponseReader endpointResponseReader;

	public RestifyEndpointRequestExecutor(HttpClientRequestFactory httpClientRequestFactory,
			EndpointRequestWriter endpointRequestWriter, EndpointResponseReader endpointResponseReader) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		this.endpointRequestWriter = endpointRequestWriter;
		this.endpointResponseReader = endpointResponseReader;
	}

	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		try (HttpResponseMessage response = doExecute(endpointRequest)) {
			return responseOf(response, endpointRequest.responseType());

		} catch (RestifyHttpException e) {
			throw e;

		} catch (Exception e) {
			throw new RestifyHttpException(e);
		}
	}

	private HttpResponseMessage doExecute(EndpointRequest endpointRequest) {
		HttpClientRequest httpClientRequest = httpClientRequestFactory.createOf(endpointRequest);

		endpointRequest.body().ifPresent(b -> endpointRequestWriter.write(endpointRequest, httpClientRequest));

		return httpClientRequest.execute();
	}

	private <T> EndpointResponse<T> responseOf(HttpResponseMessage response, JavaType responseType) {
		return endpointResponseReader.read(response, responseType);
	}
}
