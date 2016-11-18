package com.restify.http.client.call.exec;

import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public interface EndpointCallExecutableDecoratorFactory<M, T, O> extends EndpointCallExecutableProvider {

	public JavaType returnType(EndpointMethod endpointMethod);

	public EndpointCallExecutable<M, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable);
}
