package com.restify.http.client.request.interceptor;

import static com.restify.http.client.Headers.ACCEPT;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.restify.http.client.Header;
import com.restify.http.client.message.HttpMessageConverters;
import com.restify.http.client.message.HttpMessageReader;
import com.restify.http.client.request.EndpointRequest;

public class AcceptHeaderEndpointRequestInterceptor implements EndpointRequestInterceptor {

	private HttpMessageConverters messageConverters;

	public AcceptHeaderEndpointRequestInterceptor(HttpMessageConverters messageConverters) {
		this.messageConverters = messageConverters;
	}

	@Override
	public EndpointRequest intercepts(EndpointRequest endpointRequest) {
		Optional<Header> accept = endpointRequest.headers().get(ACCEPT);

		if (!accept.isPresent()) {
			Type expectedType = endpointRequest.expectedType().type();

			Collection<HttpMessageReader<Object>> convertersOfType = messageConverters.readersOf(expectedType);

			if (!convertersOfType.isEmpty()) {
				String acceptTypes = convertersOfType.stream()
						.map(converter -> converter.contentType())
							.collect(Collectors.joining(", "));

				endpointRequest.headers().add(new Header(ACCEPT, acceptTypes));
			}
		}

		return endpointRequest;
	}
}
