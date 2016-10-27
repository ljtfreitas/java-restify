package com.restify.http.client.request;

import java.net.URI;
import java.net.URISyntaxException;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.Header;
import com.restify.http.client.Headers;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.restify.http.contract.metadata.EndpointHeaderParameterResolver;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class EndpointRequestFactory {

	private final EndpointRequestInterceptorStack interceptors;

	public EndpointRequestFactory(EndpointRequestInterceptorStack interceptors) {
		this.interceptors = interceptors;
	}

	public EndpointRequest createWith(EndpointMethod endpointMethod, Object[] args) {
		return interceptors.apply(newRequest(endpointMethod, args, endpointMethod.returnType()));
	}

	public EndpointRequest createWith(EndpointMethod endpointMethod, Object[] args, JavaType responseType) {
		return interceptors.apply(newRequest(endpointMethod, args, responseType));
	}

	private EndpointRequest newRequest(EndpointMethod endpointMethod, Object[] args, JavaType responseType) {
		try {
			URI endpoint = new URI(endpointMethod.expand(args));

			Object body = endpointMethod.parameters()
					.ofBody()
						.map(p -> args[p.position()]).orElse(null);

			Headers headers = new Headers();
			endpointMethod.headers().all().stream()
				.forEach(h -> headers.add(new Header(h.name(), new EndpointHeaderParameterResolver(h.value(), endpointMethod.parameters())
						.resolve(args))));

			return new EndpointRequest(endpoint, endpointMethod.httpMethod(), headers, body, responseType);

		} catch (URISyntaxException e) {
			throw new RestifyHttpException(e);
		}
	}
}
