package com.restify.http.spring.contract.metadata;

import java.lang.reflect.Type;

import com.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.restify.http.contract.metadata.EndpointMethodQueryParameterSerializer;
import com.restify.http.contract.metadata.EndpointMethodQueryParametersSerializer;
import com.restify.http.contract.metadata.SimpleEndpointMethodParameterSerializer;
import com.restify.http.spring.contract.metadata.reflection.SpringWebJavaMethodParameterMetadata;

public class SpringWebEndpointMethodParameterSerializer {

	public static Class<? extends EndpointMethodParameterSerializer> of(SpringWebJavaMethodParameterMetadata parameter) {
		if (parameter.query()) {
			return SpringWebQueryParameterSerializer.class;
		} else {
			return SimpleEndpointMethodParameterSerializer.class;
		}
	}

	public static class SpringWebQueryParameterSerializer implements EndpointMethodParameterSerializer {

		private final EndpointMethodQueryParameterSerializer queryParameterSerializer = new EndpointMethodQueryParameterSerializer();

		private final EndpointMethodQueryParametersSerializer queryParametersSerializer = new EndpointMethodQueryParametersSerializer();

		@Override
		public String serialize(String name, Type type, Object source) {
			if (queryParametersSerializer.supports(type, source)) {
				return queryParametersSerializer.serialize(name, type, source);
			} else {
				return queryParameterSerializer.serialize(name, type, source);
			}
		}
	}
}
