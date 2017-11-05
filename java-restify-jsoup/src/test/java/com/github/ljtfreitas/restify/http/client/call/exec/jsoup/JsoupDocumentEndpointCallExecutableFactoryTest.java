package com.github.ljtfreitas.restify.http.client.call.exec.jsoup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class JsoupDocumentEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<EndpointResponse<String>, String> delegate;

	private JsoupDocumentEndpointCallExecutableFactory factory;

	private Headers headers;

	@Before
	public void setup() {
		factory = new JsoupDocumentEndpointCallExecutableFactory();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsDocument() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("document"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotDocument() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnEndpointResponseOfStringAsReturnType() throws Exception {
		assertEquals(JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, String.class)), factory.returnType(null));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithDocumentReturnTypeWhenResponseContentTypeIsHtml() throws Exception {
		headers = new Headers(new Header("Content-Type", "text/html"));

		String html = "<html><head></head><body>hello world</body></html>";

		when(delegate.execute(any(), anyVararg()))
			.thenReturn(new EndpointResponse<String>(StatusCode.ok(), headers, html));

		EndpointCallExecutable<Document, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("document")), delegate);

		Document document = executable.execute(() -> html, new Object[0]);

		verify(delegate).execute(any(), anyVararg());

		document.outputSettings().prettyPrint(false);

		assertEquals(html, document.html());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenEndpointResponseContentTypeIsNotHtml() throws Exception {
		headers = new Headers(new Header("Content-Type", "application/json"));

		String body = "{name:value}";

		when(delegate.execute(any(), anyVararg()))
			.thenReturn(new EndpointResponse<String>(StatusCode.ok(), headers, body));

		EndpointCallExecutable<Document, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("document")), delegate);

		executable.execute(() -> body, new Object[0]);
	}

	interface SomeType {

		Document document();

		String string();
	}

}
