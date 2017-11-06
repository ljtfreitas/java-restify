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

import java.util.ArrayList;
import java.util.Collection;

class MultipartFieldSerializers {

	private static final MultipartFieldSerializer<Object> FALLBACK_SERIALIZER = new SimpleMultipartFieldSerializer();

	private final Collection<MultipartFieldSerializer<?>> serializers;

	public MultipartFieldSerializers(Collection<MultipartFieldSerializer<?>> serializers) {
		this.serializers = new ArrayList<>(serializers);
	}

	public MultipartFieldSerializers() {
		serializers = new ArrayList<>();

		serializers.add(new FileMultipartFieldSerializer());
		serializers.add(new InputStreamMultipartFieldSerializer());
		serializers.add(new PathMultipartFieldSerializer());
		serializers.add(new MultipartFileFieldSerializer());

		serializers.add(new IterableMultipartFieldSerializer(serializers));
	}

	public MultipartFieldSerializer<?> of(Class<?> type) {
		return serializers.stream()
				.filter(c -> c.supports(type))
					.findFirst()
						.orElseGet(() -> FALLBACK_SERIALIZER);
	}
}
