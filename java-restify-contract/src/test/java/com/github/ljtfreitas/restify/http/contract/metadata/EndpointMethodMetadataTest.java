package com.github.ljtfreitas.restify.http.contract.metadata;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Before;
import org.junit.Test;

public class EndpointMethodMetadataTest {

	private EndpointMethodMetadata methodMetadata;

	@Before
	public void setup() throws Exception {
		methodMetadata = new EndpointMethodMetadata(MyType.class.getMethod("whatever"));	
	}
	
	@Test
	public void shouldScanMetadataAnnotationOnMethod() {
		assertTrue(methodMetadata.contains(WhateverOnMethod.class));

		assertThat(methodMetadata.all(), not(empty()));
		assertThat(methodMetadata.all(WhateverOnMethod.class), not(empty()));

		assertTrue(methodMetadata.get(WhateverOnMethod.class).isPresent());
		assertEquals("onMethod", methodMetadata.get(WhateverOnMethod.class).get().value());
	}

	@Test
	public void shouldScanMetadataAnnotationOnType() {
		assertTrue(methodMetadata.contains(WhateverOnType.class));

		assertThat(methodMetadata.all(), not(empty()));
		assertThat(methodMetadata.all(WhateverOnType.class), not(empty()));

		assertTrue(methodMetadata.get(WhateverOnType.class).isPresent());
		assertEquals("onType", methodMetadata.get(WhateverOnType.class).get().value());
	}

	@WhateverOnType("onType")
	private interface MyType {

		@WhateverOnMethod("onMethod")
		String whatever();
	}

	@Retention(RUNTIME)
	@Target(METHOD)
	@Metadata
	private @interface WhateverOnMethod {

		String value();
	}

	@Retention(RUNTIME)
	@Target(TYPE)
	@Metadata
	private @interface WhateverOnType {

		String value();
	}
}
