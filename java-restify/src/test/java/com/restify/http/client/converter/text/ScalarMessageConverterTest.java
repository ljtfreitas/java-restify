package com.restify.http.client.converter.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.client.converter.SimpleHttpResponseMessage;

public class ScalarMessageConverterTest {

	private ScalarMessageConverter converter = new ScalarMessageConverter();

	@Test
	public void shouldCanReadByteType() {
		assertTrue(converter.canRead(Byte.class));
		assertTrue(converter.canRead(byte.class));
	}

	@Test
	public void shouldReadByteValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(httpResponseMessage, Byte.class);

		assertEquals((byte) 1, value);
	}

	@Test
	public void shouldCanWriteByteType() {
		assertTrue(converter.canWrite(Byte.class));
		assertTrue(converter.canWrite(byte.class));
	}

	@Test
	public void shouldWriteByteValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((byte) 1, httpRequetMessage);

		assertEquals("1", output.toString());
	}

	@Test
	public void shouldCanReadShortType() {
		assertTrue(converter.canRead(Short.class));
		assertTrue(converter.canRead(short.class));
	}

	@Test
	public void shouldReadShortValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(httpResponseMessage, Short.class);

		assertEquals((short) 1, value);
	}

	@Test
	public void shouldCanWriteShortType() {
		assertTrue(converter.canWrite(Short.class));
		assertTrue(converter.canWrite(short.class));
	}

	@Test
	public void shouldWriteShortValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((short) 1, httpRequetMessage);

		assertEquals("1", output.toString());
	}

	@Test
	public void shouldCanReadIntegerType() {
		assertTrue(converter.canRead(Integer.class));
		assertTrue(converter.canRead(int.class));
	}

	@Test
	public void shouldReadIntegerValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(httpResponseMessage, Integer.class);

		assertEquals((int) 1, value);
	}

	@Test
	public void shouldCanWriteIntegerType() {
		assertTrue(converter.canWrite(Integer.class));
		assertTrue(converter.canWrite(int.class));
	}

	@Test
	public void shouldWriteIntegerValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((int) 1, httpRequetMessage);

		assertEquals("1", output.toString());
	}

	@Test
	public void shouldCanReadLongType() {
		assertTrue(converter.canRead(Long.class));
		assertTrue(converter.canRead(long.class));
	}

	@Test
	public void shouldReadLongValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(httpResponseMessage, Long.class);

		assertEquals((long) 1, value);
	}

	@Test
	public void shouldCanWriteLongType() {
		assertTrue(converter.canWrite(Long.class));
		assertTrue(converter.canWrite(long.class));
	}

	@Test
	public void shouldWriteLongValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((long) 1, httpRequetMessage);

		assertEquals("1", output.toString());
	}

	@Test
	public void shouldCanReadFloatType() {
		assertTrue(converter.canRead(Float.class));
		assertTrue(converter.canRead(float.class));
	}

	@Test
	public void shouldReadFloatValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1.1".getBytes()));
		Object value = converter.read(httpResponseMessage, Float.class);

		assertEquals((float) 1.1, value);
	}

	@Test
	public void shouldCanWriteFloatType() {
		assertTrue(converter.canWrite(Float.class));
		assertTrue(converter.canWrite(float.class));
	}

	@Test
	public void shouldWriteFloatValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((float) 1.1, httpRequetMessage);

		assertEquals("1.1", output.toString());
	}

	@Test
	public void shouldCanReadDoubleType() {
		assertTrue(converter.canRead(Double.class));
		assertTrue(converter.canRead(double.class));
	}

	@Test
	public void shouldReadDoubleValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("2.2".getBytes()));
		Object value = converter.read(httpResponseMessage, Double.class);

		assertEquals((double) 2.2, value);
	}

	@Test
	public void shouldCanWriteDoubleType() {
		assertTrue(converter.canWrite(Double.class));
		assertTrue(converter.canWrite(double.class));
	}

	@Test
	public void shouldWriteDoubleValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((double) 2.2, httpRequetMessage);

		assertEquals("2.2", output.toString());
	}

	@Test
	public void shouldCanReadBooleanType() {
		assertTrue(converter.canRead(Boolean.class));
		assertTrue(converter.canRead(boolean.class));
	}

	@Test
	public void shouldReadBooleanValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("true".getBytes()));
		Object value = converter.read(httpResponseMessage, Boolean.class);

		assertEquals((boolean) true, value);
	}

	@Test
	public void shouldCanWriteBooleanType() {
		assertTrue(converter.canWrite(Boolean.class));
		assertTrue(converter.canWrite(boolean.class));
	}

	@Test
	public void shouldWriteBooleanValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((boolean) false, httpRequetMessage);

		assertEquals("false", output.toString());
	}

	@Test
	public void shouldCanReadCharacterType() {
		assertTrue(converter.canRead(Character.class));
		assertTrue(converter.canRead(char.class));
	}

	@Test
	public void shouldReadCharacterValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("a".getBytes()));
		Object value = converter.read(httpResponseMessage, Character.class);

		assertEquals((char) 'a', value);
	}

	@Test
	public void shouldCanWriteCharacterType() {
		assertTrue(converter.canWrite(Character.class));
		assertTrue(converter.canWrite(char.class));
	}

	@Test
	public void shouldWriteCharacterValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((char) 'a', httpRequetMessage);

		assertEquals("a", output.toString());
	}
}
