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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class JsoupDocumentEndpointCallExecutableFactory implements EndpointCallExecutableFactory<Document, String> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Document.class);
	}

	@Override
	public EndpointCallExecutable<Document, String> create(EndpointMethod endpointMethod) {
		return new JsoupDocumentEndpointCallExecutable(endpointMethod.returnType());
	}

	private class JsoupDocumentEndpointCallExecutable implements EndpointCallExecutable<Document, String> {

		private final JavaType returnType;

		private JsoupDocumentEndpointCallExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public Document execute(EndpointCall<String> call, Object[] args) {
			return Jsoup.parse(call.execute());
		}
	}
}
