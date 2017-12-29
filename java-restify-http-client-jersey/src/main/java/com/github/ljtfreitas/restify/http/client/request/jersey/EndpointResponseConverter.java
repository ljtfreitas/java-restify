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
package com.github.ljtfreitas.restify.http.client.request.jersey;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.reflection.JavaType;

class EndpointResponseConverter {

	private final EndpointResponseErrorFallback endpointResponseErrorFallback;

	public EndpointResponseConverter(EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.endpointResponseErrorFallback = endpointResponseErrorFallback;
	}

	public <T> EndpointResponse<T> convert(Response response, EndpointRequest request) {
		Family statusFamily = response.getStatusInfo().getFamily();

		if (statusFamily == Family.SERVER_ERROR || statusFamily == Family.CLIENT_ERROR) {
			return endpointResponseErrorFallback.onError(ErrorHttpResponseMessage.from(response, request), request.responseType());

		} else {
			return doConvert(response, request.responseType());
		}
	}

	private <T> EndpointResponse<T> doConvert(Response response, JavaType responseType) {
		T responseBody = response.hasEntity() ? entityOf(response, responseType) : null;
		return new EndpointResponse<>(StatusCode.of(response.getStatus()), headersOf(response), responseBody);
	}

	private <T> T entityOf(Response response, JavaType responseType) {
		return response.readEntity(new GenericType<>(responseType.unwrap()));
	}

	private Headers headersOf(Response response) {
		return response.getHeaders().entrySet().stream()
				.reduce(new Headers(), (a, b) -> {b.getValue().forEach(c -> a.add(b.getKey(), c.toString())); return a;},
						(a, b) -> b);
	}
}
