package com.restify.http.contract.metadata;

import java.lang.reflect.Method;

public interface RestifyContractReader {

	EndpointMethod read(EndpointTarget endpointTarget, Method javaMethod);

}