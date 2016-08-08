package com.restify.http.client;

import java.net.URI;
import java.net.URISyntaxException;

import com.restify.http.RestifyHttpException;
import com.restify.http.metadata.EndpointHeaderParameterResolver;
import com.restify.http.metadata.EndpointMethod;

public class EndpointRequestFactory {

	private final EndpointMethod endpointMethod;

	public EndpointRequestFactory(EndpointMethod endpointMethod) {
		this.endpointMethod = endpointMethod;
	}

	public EndpointRequest createWith(Object[] args) {
		try {
			URI endpoint = new URI(endpointMethod.expand(args));

			Object body = endpointMethod.parameters()
					.ofBody()
						.map(p -> args[p.position()]).orElse(null);

			Headers headers = new Headers();
			endpointMethod.headers().all().stream()
				.forEach(h -> headers.add(new Header(h.name(), new EndpointHeaderParameterResolver(h.value(), endpointMethod.parameters())
						.resolve(args))));

			return new EndpointRequest(endpoint, endpointMethod.httpMethod(), headers, body, endpointMethod.returnType());

		} catch (URISyntaxException e) {
			throw new RestifyHttpException(e);
		}

	}
}
