package com.github.ljtfreitas.restify.http.client.request;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodMetadata;
import com.github.ljtfreitas.restify.http.contract.metadata.Metadata;

public class EndpointRequestMetadataTest {

	@Test
	public void shouldGetAnnotationByType() throws Exception {
		EndpointMethodMetadata methodMetadata = EndpointMethodMetadata.of(MyType.class.getMethod("bla"));

		EndpointRequestMetadata endpointRequestMetadata = new EndpointRequestMetadata(methodMetadata.all());

		Optional<Whatever> whatever = endpointRequestMetadata.get(Whatever.class);

		assertTrue(whatever.isPresent());
		assertEquals("method", whatever.get().value());
	}

	@Test
	public void shouldGetAllAnnotationsByType() throws Exception {
		EndpointMethodMetadata methodMetadata = EndpointMethodMetadata.of(MyType.class.getMethod("bla"));

		EndpointRequestMetadata endpointRequestMetadata = new EndpointRequestMetadata(methodMetadata.all());

		List<Whatever> whatevers = new ArrayList<>(endpointRequestMetadata.all(Whatever.class));

		assertThat(whatevers, hasSize(2));

		assertEquals("method", whatevers.get(0).value());
		assertEquals("type", whatevers.get(1).value());
	}

	@Whatever("type")
	private interface MyType {

		@Whatever("method")
		String bla();
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Metadata
	private @interface Whatever {

		String value();
	}
}
