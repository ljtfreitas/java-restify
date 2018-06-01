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
package com.github.ljtfreitas.restify.http.client.response.interceptor.log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;
import com.github.ljtfreitas.restify.http.client.response.interceptor.HttpClientResponseInterceptor;
import com.github.ljtfreitas.restify.util.Tryable;

public class LogHttpClientResponseInterceptor implements HttpClientResponseInterceptor {

	private static final Logger log = Logger.getLogger(LogHttpClientResponseInterceptor.class.getCanonicalName());

	@Override
	public HttpClientResponse intercepts(HttpClientResponse response) {
		if (log.isLoggable(Level.INFO)) {
			log.log(record(response));
		}

		return response;
	}

	private LogRecord record(HttpClientResponse response) {
		StringBuilder message = new StringBuilder();

		message.append("**********")
			   .append("\n")
			   .append("< " + response.status());

		response.headers().forEach(h -> message.append("\n").append("< " + h.toString()));

		if (response.available() && !response.body().empty()) {
			message.append("\n")
				   .append("< " + Tryable.of(response.body()::asString))
				   .append("\n");
		}

		message.append("<");

		return new LogRecord(Level.INFO, message.toString());
	}
}
