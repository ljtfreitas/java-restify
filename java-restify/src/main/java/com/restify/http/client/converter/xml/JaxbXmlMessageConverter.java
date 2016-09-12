package com.restify.http.client.converter.xml;

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

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;
import com.restify.http.client.converter.HttpMessageReader;
import com.restify.http.client.converter.HttpMessageWriter;

public class JaxbXmlMessageConverter<T> implements HttpMessageReader<T>, HttpMessageWriter<T> {

	private static final String APPLICATION_XML = "application/xml";

	private final Map<Class<?>, JAXBContext> contexts = new ConcurrentHashMap<>();

	@Override
	public String contentType() {
		return APPLICATION_XML;
	}

	@Override
	public boolean canRead(Type type) {
		return type instanceof Class && (((Class<?>) type).isAnnotationPresent(XmlRootElement.class)
				|| ((Class<?>) type).isAnnotationPresent(XmlType.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public T read(Type expectedType, HttpResponseMessage httpResponseMessage) {
		Class<T> expectedClassType = (Class<T>) expectedType;

		JAXBContext context = contextOf(expectedClassType);

		try {
			Unmarshaller unmarshaller = context.createUnmarshaller();

			StreamSource source = new StreamSource(httpResponseMessage.input());

			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(source);

			return expectedClassType.isAnnotationPresent(XmlRootElement.class) ? (T) unmarshaller.unmarshal(reader)
					: unmarshaller.unmarshal(reader, expectedClassType).getValue();

		} catch (JAXBException | XMLStreamException | FactoryConfigurationError e) {
			throw new RestifyHttpMessageReadException("Error on try read xml message", e);
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
	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		JAXBContext context = contextOf(body.getClass());

		try {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, httpRequestMessage.charset().name());

			marshaller.marshal(body, new StreamResult(httpRequestMessage.output()));

		} catch (JAXBException e) {
			throw new RestifyHttpMessageWriteException("Error on try write xml message", e);
		}
	}
}
