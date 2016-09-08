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
		assertTrue(converter.readerOf(Byte.class));
		assertTrue(converter.readerOf(byte.class));
	}

	@Test
	public void shouldReadByteValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(Byte.class, httpResponseMessage);

		assertEquals((byte) 1, value);
	}

	@Test
	public void shouldCanWriteByteType() {
		assertTrue(converter.writerOf(Byte.class));
		assertTrue(converter.writerOf(byte.class));
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
		assertTrue(converter.readerOf(Short.class));
		assertTrue(converter.readerOf(short.class));
	}

	@Test
	public void shouldReadShortValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(Short.class, httpResponseMessage);

		assertEquals((short) 1, value);
	}

	@Test
	public void shouldCanWriteShortType() {
		assertTrue(converter.writerOf(Short.class));
		assertTrue(converter.writerOf(short.class));
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
		assertTrue(converter.readerOf(Integer.class));
		assertTrue(converter.readerOf(int.class));
	}

	@Test
	public void shouldReadIntegerValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(Integer.class, httpResponseMessage);

		assertEquals((int) 1, value);
	}

	@Test
	public void shouldCanWriteIntegerType() {
		assertTrue(converter.writerOf(Integer.class));
		assertTrue(converter.writerOf(int.class));
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
		assertTrue(converter.readerOf(Long.class));
		assertTrue(converter.readerOf(long.class));
	}

	@Test
	public void shouldReadLongValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(Long.class, httpResponseMessage);

		assertEquals((long) 1, value);
	}

	@Test
	public void shouldCanWriteLongType() {
		assertTrue(converter.writerOf(Long.class));
		assertTrue(converter.writerOf(long.class));
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
		assertTrue(converter.readerOf(Float.class));
		assertTrue(converter.readerOf(float.class));
	}

	@Test
	public void shouldReadFloatValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("1.1".getBytes()));
		Object value = converter.read(Float.class, httpResponseMessage);

		assertEquals((float) 1.1, value);
	}

	@Test
	public void shouldCanWriteFloatType() {
		assertTrue(converter.writerOf(Float.class));
		assertTrue(converter.writerOf(float.class));
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
		assertTrue(converter.readerOf(Double.class));
		assertTrue(converter.readerOf(double.class));
	}

	@Test
	public void shouldReadDoubleValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("2.2".getBytes()));
		Object value = converter.read(Double.class, httpResponseMessage);

		assertEquals((double) 2.2, value);
	}

	@Test
	public void shouldCanWriteDoubleType() {
		assertTrue(converter.writerOf(Double.class));
		assertTrue(converter.writerOf(double.class));
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
		assertTrue(converter.readerOf(Boolean.class));
		assertTrue(converter.readerOf(boolean.class));
	}

	@Test
	public void shouldReadBooleanValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("true".getBytes()));
		Object value = converter.read(Boolean.class, httpResponseMessage);

		assertEquals((boolean) true, value);
	}

	@Test
	public void shouldCanWriteBooleanType() {
		assertTrue(converter.writerOf(Boolean.class));
		assertTrue(converter.writerOf(boolean.class));
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
		assertTrue(converter.readerOf(Character.class));
		assertTrue(converter.readerOf(char.class));
	}

	@Test
	public void shouldReadCharacterValue() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream("a".getBytes()));
		Object value = converter.read(Character.class, httpResponseMessage);

		assertEquals((char) 'a', value);
	}

	@Test
	public void shouldCanWriteCharacterType() {
		assertTrue(converter.writerOf(Character.class));
		assertTrue(converter.writerOf(char.class));
	}

	@Test
	public void shouldWriteCharacterValue() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequetMessage = new SimpleHttpRequestMessage(output);
		converter.write((char) 'a', httpRequetMessage);

		assertEquals("a", output.toString());
	}
}
