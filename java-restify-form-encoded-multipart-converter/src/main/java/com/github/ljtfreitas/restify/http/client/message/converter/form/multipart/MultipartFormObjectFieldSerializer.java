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
package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import java.util.Collection;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;

class MultipartFormObjectFieldSerializer implements MultipartFieldSerializer<MultipartFormObjectFieldHolder> {

	private final MultipartFieldSerializers serializers;

	public MultipartFormObjectFieldSerializer(Collection<MultipartFieldSerializer<?>> serializers) {
		this.serializers = new MultipartFieldSerializers(serializers);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void write(String boundary, MultipartField<MultipartFormObjectFieldHolder> field,
			HttpRequestMessage httpRequestMessage) {

		MultipartFormObjectFieldHolder holder = field.value();

		serializers.of(holder.type()).write(boundary,
				new MultipartField(holder.name(), holder.value(), holder.contentType()), httpRequestMessage);
	}

	@Override
	public boolean supports(Class<?> type) {
		return MultipartFormObjectFieldHolder.class.isAssignableFrom(type);
	}

}
