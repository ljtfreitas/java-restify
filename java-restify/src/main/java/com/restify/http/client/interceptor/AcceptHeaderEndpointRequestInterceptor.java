package com.restify.http.client.interceptor;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.stream.Collectors;

import com.restify.http.client.EndpointRequest;
import com.restify.http.client.Header;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.HttpMessageReader;

public class AcceptHeaderEndpointRequestInterceptor implements EndpointRequestInterceptor {

	private HttpMessageConverters messageConverters;

	public AcceptHeaderEndpointRequestInterceptor(HttpMessageConverters messageConverters) {
		this.messageConverters = messageConverters;
	}

	@Override
	public EndpointRequest intercepts(EndpointRequest endpointRequest) {
		Type expectedType = endpointRequest.expectedType();

		Collection<HttpMessageReader<Object>> convertersOfType = messageConverters.readersOf(expectedType);

		if (!convertersOfType.isEmpty()) {
			String acceptTypes = convertersOfType.stream()
					.map(converter -> converter.contentType())
						.collect(Collectors.joining(", "));

			endpointRequest.headers().add(new Header("Accept", acceptTypes));
		}

		return endpointRequest;
	}
}
