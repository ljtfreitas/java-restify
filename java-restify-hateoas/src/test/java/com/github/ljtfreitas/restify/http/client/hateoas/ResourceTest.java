package com.github.ljtfreitas.restify.http.client.hateoas;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ljtfreitas.restify.http.client.hateoas.JsonHateoasHalResponseTest.MyModel;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JacksonMessageConverter;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class ResourceTest {

	@Test
	public void shouldResourceBeDeserializableByJackson() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JacksonHateoasHalModule());

		JacksonMessageConverter<Object> jacksonMessageConverter = new JacksonMessageConverter<>(objectMapper);

		SimpleParameterizedType parameterizedType = new SimpleParameterizedType(Resource.class, null, MyModel.class);

		assertTrue(jacksonMessageConverter.canRead(parameterizedType));
	}

}
