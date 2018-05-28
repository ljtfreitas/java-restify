package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.contract.Form.Field;
import com.github.ljtfreitas.restify.http.contract.MultipartForm;
import com.github.ljtfreitas.restify.http.contract.MultipartForm.MultipartField;

@RunWith(MockitoJUnitRunner.class)
public class MultipartFormObjectMessageWriterTest {

	@Mock
	private HttpRequestMessage request;
	
	private MultipartFormObjectMessageWriter converter;

	private MyMultipartFormObject myMultipartFormObject;

	private RequestBody output;

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

		output = new RequestBody();
		
		when(request.body()).thenReturn(output);
		when(request.headers()).thenReturn(new Headers(Header.contentType("multipart/form-data")));
		when(request.replace(any())).thenReturn(request);
		when(request.charset()).thenReturn(Charset.forName("UTF-8"));
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

		converter.write(myMultipartFormObject, request);

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
