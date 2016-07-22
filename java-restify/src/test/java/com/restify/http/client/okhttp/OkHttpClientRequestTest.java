package com.restify.http.client.okhttp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.restify.http.RestifyProxyBuilder;
import com.restify.http.contract.BodyParameter;
import com.restify.http.contract.Header;
import com.restify.http.contract.Path;
import com.restify.http.contract.Post;

public class OkHttpClientRequestTest {

	@Test
	public void shouldSendRequest() {
		MockBinApi requestBin = new RestifyProxyBuilder()
				.client(new OkHttpClientRequestFactory())
				.target(MockBinApi.class, "http://mockbin.org/bin")
					.build();

		MyModel input = new MyModel("Tiago de Freitas Lima", 31);

		MyModel response = requestBin.sendTo("9eba6e23-7da9-44b1-9052-a9af5c0ed93f", input, "custom header");

		assertEquals("Tiago de Freitas Lima", response.name);
		assertEquals(31, response.age);
	}

	public interface MockBinApi {

		@Path("/{bin}") @Post
		@Header(name = "Content-Type", value = "application/json")
		@Header(name = "X-My-Header", value = "{myHeader}")
		public MyModel sendTo(String bin, @BodyParameter MyModel model, String myHeader);
	}

	public static class MyModel {

		String name;
		int age;

		public MyModel() {
		}

		public MyModel(String name, int age) {
			this.name = name;
			this.age = age;
		}

	}
}
