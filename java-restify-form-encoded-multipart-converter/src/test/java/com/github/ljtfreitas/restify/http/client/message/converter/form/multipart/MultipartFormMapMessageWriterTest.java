package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.contract.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class MultipartFormMapMessageWriterTest {

	private MultipartFormMapMessageWriter converter;

	private Map<String, Object> parameters;

	private ByteArrayOutputStream output;

	private File file;

	@Mock
	private HttpRequestMessage request;
	
	@Before
	public void setup() throws IOException {
		converter = new MultipartFormMapMessageWriter(new SimpleMultipartFormBoundaryGenerator("myBoundary"));

		file = File.createTempFile("myTextFile", ".txt");

		OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file));
		fileWriter.write("file content...line 1");
		fileWriter.write("\n");
		fileWriter.write("file content...line 2");
		fileWriter.flush();
		fileWriter.close();

		parameters = new LinkedHashMap<>();

		output = new ByteArrayOutputStream();

		when(request.output()).thenReturn(output);
		when(request.headers()).thenReturn(new Headers(Header.contentType("multipart/form-data")));
		when(request.replace(any())).thenReturn(request);
		when(request.charset()).thenReturn(Charset.forName("UTF-8"));
	}

	@Test
	public void shouldSerializeMapAsFormMultipartDataWithTextFields() throws IOException {
		parameters.put("name", "Tiago de Freitas Lima");
		parameters.put("numeric", Arrays.asList(1, 2));

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

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMapAsFormMultipartDataWithFileField() throws IOException {
		parameters.put("file", file);

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

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMapAsFormMultipartDataWithInputStreamField() throws IOException {
		parameters.put("inputStream", new FileInputStream(file));

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"inputStream\""
				+ "\r\n"
				+ "\r\n"
				+ "file content...line 1"
				+ "\n"
				+ "file content...line 2"
				+ "\r\n"
				+ "\r\n"
				+ "------myBoundary--";

		converter.write(parameters, request);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMapAsFormMultipartDataWithPathField() throws IOException {
		parameters.put("path", file.toPath());

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

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMapAsFormMultipartDataWithMultipartFileField() throws IOException {
		parameters.put("myMultipartFile", MultipartFile.create("", file));

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

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMapAsFormMultipartDataWithMultipartFilePathField() throws IOException {
		parameters.put("myMultipartFileAsPath", MultipartFile.create("", file.toPath()));

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

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMapAsFormMultipartDataWithMultipartFileInputStreamField() throws IOException {
		parameters.put("myMultipartFileAsInputStream", MultipartFile.create("", file.getName(), "text/plain",
				new FileInputStream(file)));

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

		assertEquals(body, output.toString());
	}
}
