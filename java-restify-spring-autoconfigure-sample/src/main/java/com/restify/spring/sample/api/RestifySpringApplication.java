package com.restify.spring.sample.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.restify.http.client.Headers;
import com.restify.http.spring.contract.FormParameters;
import com.restify.http.spring.contract.FormParameters.Parameter;

@SpringBootApplication
public class RestifySpringApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(RestifySpringApplication.class);
	}

	@Autowired
	private MyApi myApi;

	@Override
	public void run(String... arg0) throws Exception {
		MyApiResponse response = myApi.get("1234");
		System.out.println(response);

		response = myApi.post(new FormParameters(new Parameter("message", "Hello, This is my POST request!")));
		System.out.println(response);

		response = myApi.put(new FormParameters(new Parameter("message", "Hello, This is my PUT request!")));
		System.out.println(response);

		response = myApi.delete("1234");
		System.out.println(response);

		Headers headers = myApi.head();
		System.out.println(headers.get("X-MyJsonApi-Timestamp").get());

		HttpHeaders httpHeaders = myApi.options();
		System.out.println(httpHeaders.getAllow());

		FormParameters parameters = new FormParameters();
		parameters.add("destination", "/tmp/java-restify-api/");
		parameters.add("file", new ClassPathResource("/file.jpg"));

		String newFile = myApi.upload(parameters);
		System.out.println("New file created on server " + newFile);

		System.out.println("Starting async request on thread " + Thread.currentThread());
		ListenableFuture<MyApiResponse> myApiResponseOnFuture = myApi.getAsync("1234");
		myApiResponseOnFuture.addCallback(new ListenableFutureCallback<MyApiResponse>() {

			@Override
			public void onSuccess(MyApiResponse response) {
				System.out.println("MyApiResponse: " + response + ", on thread " + Thread.currentThread());
			}

			@Override
			public void onFailure(Throwable exception) {
				System.err.println("Exception on thread " + Thread.currentThread());
				exception.printStackTrace();
			}
		});
	}
}
