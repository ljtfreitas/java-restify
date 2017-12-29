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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;

public class JaxRsHttpClientEndpointRequestExecutor implements EndpointRequestExecutor {

	private final InvocationConverter invocationConverter;
	private final EndpointResponseConverter endpointResponseConverter;

	public JaxRsHttpClientEndpointRequestExecutor() {
		this(ClientBuilder.newClient());
	}

	public JaxRsHttpClientEndpointRequestExecutor(EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this(ClientBuilder.newClient(), endpointResponseErrorFallback);
	}

	public JaxRsHttpClientEndpointRequestExecutor(Configuration configuration) {
		this(ClientBuilder.newClient(configuration));
	}

	public JaxRsHttpClientEndpointRequestExecutor(Configuration configuration, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this(ClientBuilder.newClient(configuration), endpointResponseErrorFallback);
	}

	public JaxRsHttpClientEndpointRequestExecutor(Client client) {
		this(client, DefaultEndpointResponseErrorFallback.emptyOnNotFound());
	}

	public JaxRsHttpClientEndpointRequestExecutor(Client client, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.invocationConverter = new InvocationConverter(client);
		this.endpointResponseConverter = new EndpointResponseConverter(endpointResponseErrorFallback);
	}

	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		try {
			Invocation invocation = invocationConverter.convert(endpointRequest);

			Response response = invocation.invoke();

			EndpointResponse<T> endpointResponse = endpointResponseConverter.convert(response, endpointRequest);

			response.close();

			return endpointResponse;

		} catch (HttpException e) {
			throw e;

		} catch (WebApplicationException e) {
			throw new HttpMessageReadException(e);

		} catch (Exception e) {
			throw new HttpException("Error on HTTP request: [" + endpointRequest.method() + " " + endpointRequest.endpoint() + "]", e);
		}
	}
}
