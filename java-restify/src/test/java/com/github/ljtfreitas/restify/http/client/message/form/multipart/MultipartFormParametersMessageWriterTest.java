package com.github.ljtfreitas.restify.http.client.message.form.multipart;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormParametersMessageWriter;
import com.github.ljtfreitas.restify.http.client.request.SimpleHttpRequestMessage;
import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.MultipartFile;
import com.github.ljtfreitas.restify.http.contract.MultipartParameters;

public class MultipartFormParametersMessageWriterTest {

	private MultipartFormParametersMessageWriter converter;

	private MultipartParameters parameters;

	private SimpleHttpRequestMessage httpRequestMessage;
	private ByteArrayOutputStream output;

	private File file;

	@Before
	public void setup() throws IOException {
		converter = new MultipartFormParametersMessageWriter(new SimpleMultipartFormBoundaryGenerator("myBoundary"));

		parameters = new MultipartParameters();

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
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithTextFields() throws IOException {
		parameters.put("name", "Tiago de Freitas Lima");
		parameters.put("numeric", "1");
		parameters.put("numeric", "2");

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

		converter.write(parameters, httpRequestMessage);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithFileField() throws IOException {
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

		converter.write(parameters, httpRequestMessage);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithInputStreamField() throws IOException {
		parameters.put("inputStream", file.getName(), new FileInputStream(file));

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

		converter.write(parameters, httpRequestMessage);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithPathField() throws IOException {
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

		converter.write(parameters, httpRequestMessage);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithMultipartFileField() throws IOException {
		parameters.put(MultipartFile.create("myMultipartFile", file));

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

		converter.write(parameters, httpRequestMessage);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithMultipartFilePathField() throws IOException {
		parameters.put(MultipartFile.create("myMultipartFileAsPath", file.toPath()));

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

		converter.write(parameters, httpRequestMessage);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMultipartParametersAsFormMultipartDataWithMultipartFileInputStreamField() throws IOException {
		parameters.put(MultipartFile.create("myMultipartFileAsInputStream", file.getName(),
				ContentType.of("text/plain"), new FileInputStream(file)));

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

		converter.write(parameters, httpRequestMessage);

		assertEquals(body, output.toString());
	}
}
