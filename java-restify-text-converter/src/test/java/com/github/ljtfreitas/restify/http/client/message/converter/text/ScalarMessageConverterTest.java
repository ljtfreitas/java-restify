package com.github.ljtfreitas.restify.http.client.message.converter.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class ScalarMessageConverterTest {

	@Mock
	private HttpRequestMessage request;
	
	@Mock
	private HttpResponseMessage response;
	
	private ScalarMessageConverter converter = new ScalarMessageConverter();

	@Before
	public void setup() {
		when(request.charset()).thenReturn(Charset.defaultCharset());
	}

	@Test
	public void shouldCanReadByteType() {
		assertTrue(converter.canRead(Byte.class));
		assertTrue(converter.canRead(byte.class));
	}

	@Test
	public void shouldReadByteValue() {
		when(response.body()).thenReturn(new ByteArrayInputStream("1".getBytes()));
		
		Object value = converter.read(response, Byte.class);

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

		when(request.output()).thenReturn(output);

		converter.write((byte) 1, request);

		assertEquals("1", output.toString());
	}

	@Test
	public void shouldCanReadShortType() {
		assertTrue(converter.canRead(Short.class));
		assertTrue(converter.canRead(short.class));
	}

	@Test
	public void shouldReadShortValue() {
		when(response.body()).thenReturn(new ByteArrayInputStream("1".getBytes()));

		Object value = converter.read(response, Short.class);

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

		when(request.output()).thenReturn(output);
		converter.write((short) 1, request);

		assertEquals("1", output.toString());
	}

	@Test
	public void shouldCanReadIntegerType() {
		assertTrue(converter.canRead(Integer.class));
		assertTrue(converter.canRead(int.class));
	}

	@Test
	public void shouldReadIntegerValue() {
		when(response.body()).thenReturn(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(response, Integer.class);

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

		when(request.output()).thenReturn(output);
		converter.write((int) 1, request);

		assertEquals("1", output.toString());
	}

	@Test
	public void shouldCanReadLongType() {
		assertTrue(converter.canRead(Long.class));
		assertTrue(converter.canRead(long.class));
	}

	@Test
	public void shouldReadLongValue() {
		when(response.body()).thenReturn(new ByteArrayInputStream("1".getBytes()));
		Object value = converter.read(response, Long.class);

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

		when(request.output()).thenReturn(output);
		converter.write((long) 1, request);

		assertEquals("1", output.toString());
	}

	@Test
	public void shouldCanReadFloatType() {
		assertTrue(converter.canRead(Float.class));
		assertTrue(converter.canRead(float.class));
	}

	@Test
	public void shouldReadFloatValue() {
		when(response.body()).thenReturn(new ByteArrayInputStream("1.1".getBytes()));
		Object value = converter.read(response, Float.class);

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

		when(request.output()).thenReturn(output);
		converter.write((float) 1.1, request);

		assertEquals("1.1", output.toString());
	}

	@Test
	public void shouldCanReadDoubleType() {
		assertTrue(converter.canRead(Double.class));
		assertTrue(converter.canRead(double.class));
	}

	@Test
	public void shouldReadDoubleValue() {
		when(response.body()).thenReturn(new ByteArrayInputStream("2.2".getBytes()));
		Object value = converter.read(response, Double.class);

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

		when(request.output()).thenReturn(output);
		converter.write((double) 2.2, request);

		assertEquals("2.2", output.toString());
	}

	@Test
	public void shouldCanReadBooleanType() {
		assertTrue(converter.canRead(Boolean.class));
		assertTrue(converter.canRead(boolean.class));
	}

	@Test
	public void shouldReadBooleanValue() {
		when(response.body()).thenReturn(new ByteArrayInputStream("true".getBytes()));
		Object value = converter.read(response, Boolean.class);

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

		when(request.output()).thenReturn(output);
		converter.write((boolean) false, request);

		assertEquals("false", output.toString());
	}

	@Test
	public void shouldCanReadCharacterType() {
		assertTrue(converter.canRead(Character.class));
		assertTrue(converter.canRead(char.class));
	}

	@Test
	public void shouldReadCharacterValue() {
		when(response.body()).thenReturn(new ByteArrayInputStream("a".getBytes()));
		Object value = converter.read(response, Character.class);

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

		when(request.output()).thenReturn(output);
		converter.write((char) 'a', request);

		assertEquals("a", output.toString());
	}
}
