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

import java.io.IOException;

import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormObjectHolder.MultipartFormObjectFieldHolder;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.contract.MultipartForm;
import com.github.ljtfreitas.restify.http.contract.MultipartFormObject;
import com.github.ljtfreitas.restify.http.contract.MultipartFormObjects;

public class MultipartFormObjectMessageWriter extends BaseMultipartFormMessageWriter<Object> {

	public MultipartFormObjectMessageWriter() {
	}

	protected MultipartFormObjectMessageWriter(MultipartFormBoundaryGenerator boundaryGenerator) {
		super(boundaryGenerator);
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return type.isAnnotationPresent(MultipartForm.class);
	}

	@Override
	protected void doWrite(String boundary, Object body, HttpRequestMessage httpRequestMessage) throws IOException {
		MultipartFormObject formObject = MultipartFormObjects.cache().of(body.getClass());

		new MultipartFormObjectHolder(formObject, body)
			.fields()
				.forEach(holder -> this.doWrite(boundary, body, httpRequestMessage, holder));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doWrite(String boundary, Object body, HttpRequestMessage httpRequestMessage, MultipartFormObjectFieldHolder holder) {
		MultipartField multipartField = new MultipartField<>(holder.name(), holder);
		serializers.of(MultipartFormObjectFieldHolder.class).write(boundary, multipartField, httpRequestMessage);
	}
}
