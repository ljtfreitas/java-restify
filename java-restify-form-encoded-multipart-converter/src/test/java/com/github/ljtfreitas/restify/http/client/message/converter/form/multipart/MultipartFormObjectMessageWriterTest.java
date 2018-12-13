package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedByteArrayHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.contract.Form;
import com.github.ljtfreitas.restify.http.contract.Form.Field;
import com.github.ljtfreitas.restify.http.contract.MultipartForm;
import com.github.ljtfreitas.restify.http.contract.MultipartForm.MultipartField;

@RunWith(MockitoJUnitRunner.class)
public class MultipartFormObjectMessageWriterTest {

	@Mock
	private HttpRequestMessage request;
	
	private MultipartFormObjectMessageWriter converter;

	private MyMultipartFormObject myMultipartFormObject;

	private BufferedByteArrayHttpRequestBody output;

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

		output = new BufferedByteArrayHttpRequestBody();
		
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

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeComplexMultipartFormObjectWithNestedFieldsAsFormMultipartData() throws IOException {
		ComplexMultipartFormObject complexMultipartFormObject = new ComplexMultipartFormObject();
		complexMultipartFormObject.name = "Tiago de Freitas Lima";
		complexMultipartFormObject.file = file;

		NestedFormObject nested = new NestedFormObject();
		nested.name = "Tiago de Freitas Lima";
		nested.age = 33;
		nested.values = Arrays.asList("value1", "value2");
		complexMultipartFormObject.nested = nested;

		Date timestamp = new Date();

		OtherFormObject other = new OtherFormObject();
		other.timestamp = timestamp;
		complexMultipartFormObject.other = other;

		complexMultipartFormObject.others = Arrays.asList(other);

		String body = "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"form.name\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "Tiago de Freitas Lima"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"form.file\"; filename=\"" + file.getName() + "\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "file content...line 1"
			 + "\n"
			 + "file content...line 2"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"form.whatever.nested.name\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "Tiago de Freitas Lima"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"form.whatever.nested.age\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "33"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"form.whatever.nested.values[0]\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "value1"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"form.whatever.nested.values[1]\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + "value2"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"form.other.timestamp\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + timestamp.getTime()
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"form.others[0].timestamp\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + timestamp.getTime()
			 + "\r\n"
			 + "\r\n"
			 + "------myBoundary--";

		converter.write(complexMultipartFormObject, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeMultipartFormObjectAsFormMultipartDataUsingCustomizedContentType() throws IOException {
		MyContentFormObject content = new MyContentFormObject();
		content.customized = "whatever";

		Date timestamp = new Date();
		content.timestamp = timestamp;

		String body = "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"customized\""
			 + "\r\n"
			 + "Content-Type: application/json"
			 + "\r\n"
			 + "\r\n"
			 + "whatever"
			 + "\r\n"
			 + "------myBoundary"
			 + "\r\n"
			 + "Content-Disposition: form-data; name=\"timestamp\""
			 + "\r\n"
			 + "Content-Type: text/plain"
			 + "\r\n"
			 + "\r\n"
			 + timestamp.getTime()
			 + "\r\n"
			 + "\r\n"
			 + "------myBoundary--";

		converter.write(content, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@Test
	public void shouldSerializeFormObjectAsFormMultipartData() throws IOException {
		Date timestamp = new Date();

		OtherFormObject form = new OtherFormObject();
		form.timestamp = timestamp;

		String body = "------myBoundary"
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"timestamp\""
				+ "\r\n"
				+ "Content-Type: text/plain"
				+ "\r\n"
				+ "\r\n"
				+ timestamp.getTime()
				+ "\r\n"
				+ "\r\n"
				+ "------myBoundary--";

		converter.write(form, request);

		assertEquals(body, new String(output.asBytes()));
	}

	@MultipartForm
	static class MyMultipartFormObject {

		@Field
		String name;

		@MultipartField
		File file;
	}

	@MultipartForm("form")
	static class ComplexMultipartFormObject {

		@Field
		String name;

		@MultipartField
		File file;

		@Field("whatever")
		NestedFormObject nested;

		@Field
		OtherFormObject other;

		@MultipartField(indexed = true)
		Collection<OtherFormObject> others;
	}

	@Form("nested")
	static class NestedFormObject {

		@Field
		String name;

		@Field
		int age;

		@Field(indexed = true)
		Collection<String> values;
	}

	@Form
	static class OtherFormObject {

		@Field(serializer = TimestampFieldSerializer.class)
		Date timestamp;

	}

	@MultipartForm
	static class MyContentFormObject {

		@MultipartField(contentType = "application/json")
		String customized;

		@MultipartField(serializer = TimestampFieldSerializer.class)
		Date timestamp;
	}
}
