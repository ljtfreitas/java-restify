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
package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

public class OctetSerializableMessageConverter<T extends Serializable> implements OctetStreamMessageConverter<T> {

	@Override
	public boolean canRead(Type type) {
		return type instanceof Class && Serializable.class.isAssignableFrom((Class<?>) type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T read(HttpResponseMessage httpResponseMessage, Type expectedType) throws HttpMessageReadException {
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(httpResponseMessage.body());
			return (T) objectInputStream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			throw new HttpMessageReadException(e);
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return Serializable.class.isAssignableFrom((Class<?>) type);
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws HttpMessageWriteException {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(httpRequestMessage.body());
			objectOutputStream.writeObject(body);

			objectOutputStream.flush();
			objectOutputStream.close();

		} catch (IOException e) {
			throw new HttpMessageWriteException(e);
		}
	}
}
