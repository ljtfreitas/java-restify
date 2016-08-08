package com.restify.http.metadata.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

import com.restify.http.contract.BodyParameter;
import com.restify.http.contract.HeaderParameter;
import com.restify.http.contract.PathParameter;
import com.restify.http.contract.QueryParameters;
import com.restify.http.metadata.EndpointMethodParameterSerializer;
import com.restify.http.metadata.Parameters;

public class JavaMethodParameterMetadataTest {

	@Test
	public void shouldReadMetadataOfSimpleParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("method", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("path", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.ofPath());
	}

	@Test
	public void shouldReadMetadataOfAnnotatedPathParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithAnnotatedPathParameter", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("path", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.ofPath());
	}

	@Test
	public void shouldReadMetadataOfCustomizedNamePathParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithCustomizedNamePathParameter", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("otherName", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.ofPath());
	}

	@Test
	public void shouldReadMetadataOfHeaderParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithHeaderParameter", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("header", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.ofHeader());
	}

	@Test
	public void shouldReadMetadataOfCustomizedNameHeaderParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithCustomizedNameHeaderParameter", new Class[]{String.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("otherName", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.ofHeader());
	}

	@Test
	public void shouldReadMetadataOfBodyParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithBodyParameter", new Class[]{Object.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("body", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.ofBody());
	}

	@Test
	public void shouldReadMetadataOfQueryStringParameter() throws Exception {
		Method javaMethod = MyApiType.class.getMethod("methodWithQueryStringParameter", new Class[]{Parameters.class});

		JavaMethodParameterMetadata javaMethodParameterMetadata = new JavaMethodParameterMetadata(javaMethod.getParameters()[0]);

		assertEquals("parameters", javaMethodParameterMetadata.name());
		assertTrue(javaMethodParameterMetadata.ofQuery());
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
		public String serialize(Object source) {
			return null;
		}
	}
}
