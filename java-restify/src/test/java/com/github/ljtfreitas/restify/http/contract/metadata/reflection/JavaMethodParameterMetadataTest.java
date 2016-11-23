package com.github.ljtfreitas.restify.http.contract.metadata.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.BodyParameter;
import com.github.ljtfreitas.restify.http.contract.HeaderParameter;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.PathParameter;
import com.github.ljtfreitas.restify.http.contract.QueryParameters;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaMethodParameterMetadata;

public class JavaMethodParameterMetadataTest {

	@Test
	public void shouldReadMetadataOfSimpleParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("method", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("path", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.path());
	}

	@Test
	public void shouldReadMetadataOfAnnotatedPathParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithAnnotatedPathParameter", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("path", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.path());
	}

	@Test
	public void shouldReadMetadataOfCustomizedNamePathParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithCustomizedNamePathParameter", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("otherName", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.path());
	}

	@Test
	public void shouldReadMetadataOfHeaderParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithHeaderParameter", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("header", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.header());
	}

	@Test
	public void shouldReadMetadataOfCustomizedNameHeaderParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithCustomizedNameHeaderParameter", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("otherName", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.header());
	}

	@Test
	public void shouldReadMetadataOfBodyParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithBodyParameter", new Class[]{Object.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("body", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.body());
	}

	@Test
	public void shouldReadMetadataOfQueryStringParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithQueryStringParameter", new Class[]{Parameters.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("parameters", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.query());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenParameterHasMultiplesAnnotations() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("parameterWithMultiplesAnnotations", new Class[]{String.class});

		new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);
	}

	interface MyApiType {

		public String method(String path);

		public String methodWithAnnotatedPathParameter(@PathParameter String path);

		public String methodWithCustomizedNamePathParameter(@PathParameter("otherName") String path);

		public String methodWithCustomizedPathParameterSerializer(@PathParameter(serializer = StubEndpointMethodParameterSerializer.class) String path);

		public String methodWithHeaderParameter(@HeaderParameter String header);

		public String methodWithCustomizedNameHeaderParameter(@HeaderParameter("otherName") String header);

		public String methodWithBodyParameter(@BodyParameter Object body);

		public String methodWithQueryStringParameter(@QueryParameters Parameters parameters);

		public String parameterWithMultiplesAnnotations(@PathParameter @HeaderParameter @BodyParameter String parameter);
	}

	class StubEndpointMethodParameterSerializer implements EndpointMethodParameterSerializer {
		@Override
		public String serialize(String name, Type type, Object source) {
			return null;
		}
	}
}
