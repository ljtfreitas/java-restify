package com.restify.http.client.converter.xml;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.converter.HttpMessageConverter;

public class JaxbXmlMessageConverter implements HttpMessageConverter {

	private final Map<Class<?>, JAXBContext> contexts = new ConcurrentHashMap<>();

	@Override
	public String contentType() {
		return "application/xml";
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return type.isAnnotationPresent(XmlRootElement.class);
	}

	@Override
	public void write(Object body, HttpRequestMessage httpRequestMessage) {
		JAXBContext context = contextOf(body.getClass());

		try {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, httpRequestMessage.charset());

			marshaller.marshal(body, new StreamResult(httpRequestMessage.output()));

		} catch (JAXBException e) {
			throw new RestifyHttpException(e);
		}
	}


	@Override
	public boolean canRead(Class<?> type) {
		return type.isAnnotationPresent(XmlRootElement.class) || type.isAnnotationPresent(XmlType.class);
	}

	@Override
	public Object read(Class<?> expectedType, HttpResponseMessage httpResponseMessage) {
		JAXBContext context = contextOf(expectedType);

		try {
			Unmarshaller unmarshaller = context.createUnmarshaller();

			StreamSource source = new StreamSource(httpResponseMessage.input());

			return expectedType.isAnnotationPresent(XmlRootElement.class) ? unmarshaller.unmarshal(source)
					: unmarshaller.unmarshal(source, expectedType).getValue();

		} catch (JAXBException e) {
			throw new RestifyHttpException(e);
		}
	}

	private JAXBContext contextOf(Class<?> type) {
		return contexts.compute(type, (k, v) -> v == null ? newContextOf(k) : v);
	}

	private JAXBContext newContextOf(Class<?> type) {
		try {
			return JAXBContext.newInstance(type);
		} catch (JAXBException e) {
			throw new RestifyHttpException(e);
		}
	}

}
