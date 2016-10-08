package com.restify.http.client.request.interceptor;

import static com.restify.http.client.Headers.ACCEPT;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import com.restify.http.client.Header;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.contract.ContentType;

public class AcceptHeaderEndpointRequestInterceptor implements EndpointRequestInterceptor {

	private Collection<ContentType> contentTypes = new LinkedHashSet<>();

	public AcceptHeaderEndpointRequestInterceptor(String...contentTypes) {
		this.contentTypes.addAll(Arrays.stream(contentTypes).map(ContentType::of).collect(Collectors.toSet()));
	}

	public AcceptHeaderEndpointRequestInterceptor(ContentType...contentTypes) {
		this.contentTypes.addAll(Arrays.stream(contentTypes).collect(Collectors.toSet()));
	}

	@Override
	public EndpointRequest intercepts(EndpointRequest endpointRequest) {
		Optional<Header> accept = endpointRequest.headers().get(ACCEPT);

		if (!accept.isPresent()) {
			String acceptTypes = contentTypes.stream().map(ContentType::name).collect(Collectors.joining(", "));
			endpointRequest.headers().add(new Header(ACCEPT, acceptTypes));
		}

		return endpointRequest;
	}
}
