package com.github.ljtfreitas.restify.http.client.call.exec.jsoup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;

@RunWith(MockitoJUnitRunner.class)
public class JsoupDocumentEndpointCallExecutableFactoryTest {

	private JsoupDocumentEndpointCallExecutableFactory factory;

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
	public void shouldCreateExecutableFromEndpointMethodWithDocumentReturnTypeUsingHtmlResponse() throws Exception {
		String html = "<html><head></head><body>hello world</body></html>";

		EndpointCallExecutable<Document, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("document")));

		Document document = executable.execute(() -> html, new Object[0]);

		document.outputSettings().prettyPrint(false);

		assertEquals(html, document.html());
	}

	interface SomeType {

		Document document();

		String string();
	}

	public class SimpleEndpointMethod extends EndpointMethod {

		public SimpleEndpointMethod(Method javaMethod) {
			super(javaMethod, "/", "GET");
		}

		public SimpleEndpointMethod(Method javaMethod, EndpointMethodParameters parameters) {
			super(javaMethod, "/", "GET", parameters);
		}
	}

}
