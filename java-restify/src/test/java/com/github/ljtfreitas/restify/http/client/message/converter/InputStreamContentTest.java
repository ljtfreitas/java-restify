package com.github.ljtfreitas.restify.http.client.message.converter;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class InputStreamContentTest {

	@Test
	public void shouldWriteStreamContentToOutput() throws IOException {
		ByteArrayInputStream source = new ByteArrayInputStream("source".getBytes());

		InputStreamContent content = new InputStreamContent(source);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		content.transferTo(output);

		output.flush();
		output.close();

		assertEquals("source", new String(output.toByteArray()));
	}

}
