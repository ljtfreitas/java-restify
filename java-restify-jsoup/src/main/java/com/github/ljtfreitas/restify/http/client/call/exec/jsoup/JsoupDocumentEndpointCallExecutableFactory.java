/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.call.exec.jsoup;

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

public class JsoupDocumentEndpointCallExecutableFactory implements EndpointCallExecutableDecoratorFactory<Document, EndpointResponse<String>, String> {

	private static final JavaType DEFAULT_RETURN_TYPE = JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, String.class));

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Document.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return DEFAULT_RETURN_TYPE;
	}

	@Override
	public EndpointCallExecutable<Document, String> create(EndpointMethod endpointMethod, EndpointCallExecutable<EndpointResponse<String>, String> delegate) {
		return new JsoupDocumentEndpointCallExecutable(delegate);
	}

	private class JsoupDocumentEndpointCallExecutable implements EndpointCallExecutable<Document, String> {

		private final EndpointCallExecutable<EndpointResponse<String>, String> delegate;

		private JsoupDocumentEndpointCallExecutable(EndpointCallExecutable<EndpointResponse<String>, String> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Document execute(EndpointCall<String> call, Object[] args) {
			EndpointResponse<String> response = delegate.execute(call, args);

			Optional<ContentType> contentType = response.headers().get(Headers.CONTENT_TYPE).map(h -> ContentType.of(h.value()));
			isTrue(contentType.isPresent() && contentType.get().is("text/html"),
					"Only Content-Type [text/html] is acceptable from Jsoup. The Content-Type of HTTP response is [" + contentType.get() + "]");

			return Jsoup.parse(response.body());
		}
	}
}
