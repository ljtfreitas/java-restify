package com.restify.http.client.call;

class EndpointCallDecorator<T> implements EndpointCall<EndpointCall<T>> {

	private final EndpointCall<T> delegate;

	public EndpointCallDecorator(EndpointCall<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public EndpointCall<T> execute() {
		return delegate;
	}
}
