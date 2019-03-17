package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
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
import com.github.ljtfreitas.restify.http.client.message.request.BufferedByteArrayHttpRequestBody;
import com.github.ljtfreitas.restify.http.contract.MultipartFile;
import com.github.ljtfreitas.restify.http.contract.MultipartParameters;

@RunWith(MockitoJUnitRunner.class)
public class MultipartFormParametersMessageWriterTest {

	@Mock
	private HttpRequestMessage request;
	
	private MultipartFormParametersMessageWriter converter;

	private BufferedByteArrayHttpRequestBody output;

	private File file;

	@Before
	public void setup() throws IOException {
		converter = new MultipartFormParametersMessageWriter(new SimpleMultipartFormBoundaryGenerator("myBoundary"));

		file = File.createTempFile("myTextFile", ".txt");

		OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file));
		fileWriter.write("file content...line 1");
		fileWriter.write("\n");
		fileWriter.write("file content...line 2");
		fileWriter.flush();
		fileWriter.close();

		output = new BufferedByteArrayHttpRequestBody();
		
		when(request.body()).thenReturn(output);
		when(request.headers()).thenReturn(new Headers(Header.contentType("multipart/form-data")));
		when(request.replace(any())).thenReturn(request);
		when(request.charset()).thenReturn(Charset.forName("UTF-8"));
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithTextFields() throws IOException {
		MultipartParameters parameters = new MultipartParameters()
			.put("name", "Tiago de Freitas Lima")
			.put("numeric", "1")
			.put("numeric", "2");

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
			 + "Content-Disposition: form-data; name=\"numeric\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "1"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"numeric\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "2"
			 + "\r\n"
			 + "\r\n"
			 + "------myBoundary--";

		converter.write(parameters, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithFileField() throws IOException {
		MultipartParameters parameters = new MultipartParameters()
			.put("file", file);

		String body = "------myBoundary"
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

		converter.write(parameters, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithInputStreamField() throws IOException {
		MultipartParameters parameters = new MultipartParameters()
			.put("inputStream", file.getName(), new FileInputStream(file));

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"inputStream\"; filename=\"" + file.getName() + "\""
				+ "\r\n"
				+ "\r\n"
				+ "file content...line 1"
				+ "\n"
				+ "file content...line 2"
				+ "\r\n"
				+ "\r\n"
				+ "------myBoundary--";

		converter.write(parameters, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithPathField() throws IOException {
		MultipartParameters parameters = new MultipartParameters()
			.put("path", file.toPath());

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"path\"; filename=\"" + file.getName() + "\""
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

		converter.write(parameters, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithMultipartFileField() throws IOException {
		MultipartParameters parameters = new MultipartParameters()
			.put(MultipartFile.create("myMultipartFile", file));

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"myMultipartFile\"; filename=\"" + file.getName() + "\""
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

		converter.write(parameters, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithMultipartFilePathField() throws IOException {
		MultipartParameters parameters = new MultipartParameters()
			.put(MultipartFile.create("myMultipartFileAsPath", file.toPath()));

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"myMultipartFileAsPath\"; filename=\"" + file.getName() + "\""
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

		converter.write(parameters, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithMultipartFileInputStreamField() throws IOException {
		MultipartParameters parameters = new MultipartParameters()
			.put(MultipartFile.create("myMultipartFileAsInputStream", file.getName(), "text/plain", new FileInputStream(file)));

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"myMultipartFileAsInputStream\"; filename=\"" + file.getName() + "\""
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

		converter.write(parameters, request);

		assertEquals(body, new String(output.asBytes()));
	}
}
