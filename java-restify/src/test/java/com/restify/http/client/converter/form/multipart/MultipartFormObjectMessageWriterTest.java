package com.restify.http.client.converter.form.multipart;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.contract.Form.Field;
import com.restify.http.contract.MultipartForm;
import com.restify.http.contract.MultipartForm.MultipartField;

public class MultipartFormObjectMessageWriterTest {

	private MultipartFormObjectMessageWriter converter;

	private MyMultipartFormObject myMultipartFormObject;

	private SimpleHttpRequestMessage httpRequestMessage;
	private ByteArrayOutputStream output;

	private File file;

	@Before
	public void setup() throws IOException {
		converter = new MultipartFormObjectMessageWriter(new SimpleMultipartFormBoundaryGenerator("myBoundary"));

		myMultipartFormObject = new MyMultipartFormObject();

		file = File.createTempFile("myTextFile", ".txt");

		OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file));
		fileWriter.write("file content...line 1");
		fileWriter.write("\n");
		fileWriter.write("file content...line 2");
		fileWriter.flush();
		fileWriter.close();

		output = new ByteArrayOutputStream();
		httpRequestMessage = new SimpleHttpRequestMessage(output);
	}

	@Test
	public void shouldSerializeMultipartFormObjectAsFormMultipartData() throws IOException {
		myMultipartFormObject.name = "Tiago de Freitas Lima";
		myMultipartFormObject.file = file;

		String body = "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"name\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "Tiago de Freitas Lima"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "file content...line 1"
			 + "\n"
			 + "file content...line 2"
			 + "\r\n"
			 + "\r\n"
			 + "------myBoundary--";

		converter.write(myMultipartFormObject, httpRequestMessage);

		assertEquals(body, output.toString());
	}

	@MultipartForm
	static class MyMultipartFormObject {

		@Field
		private String name;

		@MultipartField
		private File file;
	}
}
