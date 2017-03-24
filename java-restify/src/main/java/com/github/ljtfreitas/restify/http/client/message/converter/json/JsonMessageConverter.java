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
package com.github.ljtfreitas.restify.http.client.message.converter.json;

import com.github.ljtfreitas.restify.http.client.message.HttpMessageReader;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageWriter;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaClassDiscovery;

public abstract class JsonMessageConverter<T> implements HttpMessageReader<T>, HttpMessageWriter<T> {

	private static final String APPLICATION_JSON = "application/json";

	@Override
	public String contentType() {
		return APPLICATION_JSON;
	}

	public static JsonMessageConverter<?> available() {
		return  JavaClassDiscovery.present("com.fasterxml.jackson.databind.ObjectMapper") ? new JacksonMessageConverter<>()
					: JavaClassDiscovery.present("com.google.gson.Gson") ? new GsonMessageConverter<>()
						: JavaClassDiscovery.present("javax.json.Json") ? new JsonpMessageConverter()
								:  new FallbackJsonMessageConverter<>();
	}
}
