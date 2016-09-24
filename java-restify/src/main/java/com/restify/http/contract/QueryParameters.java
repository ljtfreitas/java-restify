package com.restify.http.contract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.restify.http.contract.metadata.EndpointMethodQueryParametersSerializer;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter
public @interface QueryParameters {

	Class<? extends EndpointMethodParameterSerializer> serializer() default EndpointMethodQueryParametersSerializer.class;
}
