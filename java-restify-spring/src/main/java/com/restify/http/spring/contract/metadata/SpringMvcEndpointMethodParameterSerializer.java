package com.restify.http.spring.contract.metadata;

import java.lang.reflect.Type;

import com.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.restify.http.contract.metadata.EndpointMethodQueryParameterSerializer;
import com.restify.http.contract.metadata.EndpointMethodQueryParametersSerializer;
import com.restify.http.contract.metadata.SimpleEndpointMethodParameterSerializer;
import com.restify.http.spring.contract.metadata.reflection.SpringMvcJavaMethodParameterMetadata;

public class SpringMvcEndpointMethodParameterSerializer {

	public static Class<? extends EndpointMethodParameterSerializer> of(SpringMvcJavaMethodParameterMetadata parameter) {
		if (parameter.ofQuery()) {
			return SpringMvcQueryParameterSerializer.class;
		} else {
			return SimpleEndpointMethodParameterSerializer.class;
		}
	}

	public static class SpringMvcQueryParameterSerializer implements EndpointMethodParameterSerializer {

		private final EndpointMethodQueryParametersSerializer queryParametersSerializer = new EndpointMethodQueryParametersSerializer();
		private final EndpointMethodQueryParameterSerializer queryParameterSerializer = new EndpointMethodQueryParameterSerializer();

		@Override
		public String serialize(String name, Type type, Object source) {
			try {
				return queryParametersSerializer.serialize(name, type, source);
			} catch (IllegalArgumentException e) {
				return queryParameterSerializer.serialize(name, type, source);
			}
		}
	}
}
