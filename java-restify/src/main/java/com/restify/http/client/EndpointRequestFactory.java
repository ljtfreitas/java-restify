package com.restify.http.client;

import java.net.MalformedURLException;
import java.net.URL;

import com.restify.http.RestifyHttpException;
import com.restify.http.metadata.EndpointHeaderParameterResolver;
import com.restify.http.metadata.EndpointMethod;

public class EndpointRequestFactory {

	private final EndpointMethod endpointMethod;

	public EndpointRequestFactory(EndpointMethod endpointMethod) {
		this.endpointMethod = endpointMethod;
	}

	public EndpointRequest buildWith(Object[] args) {
		try {
			URL endpoint = new URL(endpointMethod.expand(args));

			Object body = endpointMethod.parameters()
					.ofBody()
						.map(p -> args[p.position()]).orElse(null);

			Headers headers = new Headers();
			endpointMethod.headers().all().stream()
				.forEach(h -> headers.add(new Header(h.name(), new EndpointHeaderParameterResolver(h.value(), endpointMethod.parameters())
						.resolve(args))));

			endpointMethod.parameters()
				.ofQueryString()
					.map(p -> args[p.position()]).orElse(null);

			return new EndpointRequest(endpoint, endpointMethod.httpMethod(), headers, body, endpointMethod.returnType());

		} catch (MalformedURLException e) {
			throw new RestifyHttpException(e);
		}

	}
}
