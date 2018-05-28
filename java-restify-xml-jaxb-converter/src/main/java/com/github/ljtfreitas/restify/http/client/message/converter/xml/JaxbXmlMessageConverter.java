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
package com.github.ljtfreitas.restify.http.client.message.converter.xml;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

public class JaxbXmlMessageConverter<T> implements XmlMessageConverter<T> {

	private final Map<Class<?>, JAXBContext> contexts = new ConcurrentHashMap<>();

	@Override
	public boolean canRead(Type type) {
		return type instanceof Class && (((Class<?>) type).isAnnotationPresent(XmlRootElement.class)
				|| ((Class<?>) type).isAnnotationPresent(XmlType.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public T read(HttpResponseMessage httpResponseMessage, Type expectedType) throws HttpMessageReadException {
		Class<T> expectedClassType = (Class<T>) expectedType;

		JAXBContext context = contextOf(expectedClassType);

		try {
			Unmarshaller unmarshaller = context.createUnmarshaller();

			StreamSource source = new StreamSource(httpResponseMessage.body());

			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(source);

			return expectedClassType.isAnnotationPresent(XmlRootElement.class) ? (T) unmarshaller.unmarshal(reader)
					: unmarshaller.unmarshal(reader, expectedClassType).getValue();

		} catch (JAXBException | XMLStreamException | FactoryConfigurationError e) {
			throw new HttpMessageReadException("Error on try read xml message", e);
		}
	}

	private JAXBContext contextOf(Class<?> type) {
		return contexts.compute(type, (k, v) -> v == null ? newContextOf(k) : v);
	}

	private JAXBContext newContextOf(Class<?> type) {
		try {
			return JAXBContext.newInstance(type);
		} catch (JAXBException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return type.isAnnotationPresent(XmlRootElement.class);
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws HttpMessageWriteException {
		JAXBContext context = contextOf(body.getClass());

		try {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, httpRequestMessage.charset().name());

			marshaller.marshal(body, new StreamResult(httpRequestMessage.body()));

			httpRequestMessage.body().flush();
			httpRequestMessage.body().close();

		} catch (JAXBException | IOException e) {
			throw new HttpMessageWriteException("Error on try write xml message", e);
		}
	}
}
