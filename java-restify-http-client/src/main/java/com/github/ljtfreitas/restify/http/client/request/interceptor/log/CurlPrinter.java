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
package com.github.ljtfreitas.restify.http.client.request.interceptor.log;

import com.github.ljtfreitas.restify.http.client.message.response.BufferedHttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.ByteArrayHttpResponseBody;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;
import com.github.ljtfreitas.restify.util.Try;

public class CurlPrinter {

	public String print(HttpClientRequest request) {
		StringBuilder message = new StringBuilder();

		message.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>").append("\n").append("> " + request.method() + " " + request.uri());

		request.headers().forEach(h -> message.append("\n").append("> " + h.toString()));

		if (!request.body().empty()) {
			message.append("\n").append("> " + new String(request.body().asBytes()));
		}

		message.append("\n").append(">");

		return message.toString();
	}

	public String print(HttpClientResponse response) {
		StringBuilder message = new StringBuilder();

		message.append("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<").append("\n").append("< " + response.status());

		response.headers().forEach(h -> message.append("\n").append("< " + h.toString()));

		BufferedHttpResponseBody bufferedHttpResponseBody = ByteArrayHttpResponseBody.of(response.body());

		if (response.available() && !bufferedHttpResponseBody.empty()) {
			message.append("\n").append("< " + Try.of(bufferedHttpResponseBody::asString).get());
		}

		message.append("\n").append("<");

		return message.toString();
	}
}