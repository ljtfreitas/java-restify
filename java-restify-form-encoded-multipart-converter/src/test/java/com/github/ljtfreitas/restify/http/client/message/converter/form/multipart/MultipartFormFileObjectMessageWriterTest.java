package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.contract.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class MultipartFormFileObjectMessageWriterTest {

	@Mock
	private HttpRequestMessage request;

	private MultipartFormFileObjectMessageWriter converter;

	private MultipartFile multipartFile;

	private RequestBody output;

	private File file;

	@Before
	public void setup() throws IOException {
		converter = new MultipartFormFileObjectMessageWriter(new SimpleMultipartFormBoundaryGenerator("myBoundary"));

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
	}

	@Test
	public void shouldSerializeMultipartFileObjectAsFormMultipartData() throws IOException {
		multipartFile = MultipartFile.create("myFile", file);

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"myFile\"; filename=\"" + file.getName() + "\""
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

		converter.write(multipartFile, request);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMultipartFileObjectWithPathAsFormMultipartData() throws IOException {
		multipartFile = MultipartFile.create("myFileAsPath", file.toPath());

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"myFileAsPath\"; filename=\"" + file.getName() + "\""
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

		converter.write(multipartFile, request);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMultipartFileObjectWithInputStreamAsFormMultipartData() throws IOException {
		multipartFile = MultipartFile.create("myFileAsInputStream", "whateverFileName", "text/plain",
				new FileInputStream(file));

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"myFileAsInputStream\"; filename=\"whateverFileName\""
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

		converter.write(multipartFile, request);

		assertEquals(body, output.toString());
	}
}
