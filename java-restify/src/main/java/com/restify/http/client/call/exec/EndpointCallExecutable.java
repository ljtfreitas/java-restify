package com.restify.http.client.call.exec;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.contract.metadata.reflection.JavaType;

public interface EndpointCallExecutable<M, T> {

	public JavaType returnType();

	public M execute(EndpointCall<T> call, Object[] args);
}
