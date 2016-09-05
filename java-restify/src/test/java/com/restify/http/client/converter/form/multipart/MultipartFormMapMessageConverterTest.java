package com.restify.http.client.converter.form.multipart;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.contract.MultipartFile;
import com.restify.http.contract.MultipartFile.ContentType;

public class MultipartFormMapMessageConverterTest {

	private MultipartFormMapMessageConverter converter;

	private Map<String, Object> parameters;

	private SimpleHttpRequestMessage httpRequestMessage;
	private ByteArrayOutputStream output;

	private File file;

	@Before
	public void setup() throws IOException {
		converter = new MultipartFormMapMessageConverter(new SimpleMultipartFormBoundaryGenerator("myBoundary"));

		file = File.createTempFile("myTextFile", ".txt");

		OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file));
		fileWriter.write("file content...line 1");
		fileWriter.write("\n");
		fileWriter.write("file content...line 2");
		fileWriter.flush();
		fileWriter.close();

		parameters = new LinkedHashMap<>();

		output = new ByteArrayOutputStream();
		httpRequestMessage = new SimpleHttpRequestMessage(output);
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
			 + "Tiago+de+Freitas+Lima"
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

		converter.write(parameters, httpRequestMessage);

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

		converter.write(parameters, httpRequestMessage);

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

		converter.write(parameters, httpRequestMessage);

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

		converter.write(parameters, httpRequestMessage);

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

		converter.write(parameters, httpRequestMessage);

		assertEquals(body, output.toString());
	}

	@Test
	public void shouldSerializeMapAsFormMultipartDataWithMultipartFileInputStreamField() throws IOException {
		parameters.put("myMultipartFileAsInputStream", MultipartFile.create("", file.getName(),
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
