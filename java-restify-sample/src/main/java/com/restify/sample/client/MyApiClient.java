package com.restify.sample.client;

import com.restify.http.RestifyProxyBuilder;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.ContentType;
import com.restify.http.contract.MultipartFile;
import com.restify.http.contract.MultipartParameters;
import com.restify.sample.api.MyApiResponse;

public class MyApiClient {

	public static void main(String[] args) {
		MyApi myApi = new RestifyProxyBuilder()
				.target(MyApi.class, "http://localhost:8080")
				.build();

		System.out.println(myApi.getAs("xml"));
		System.out.println(myApi.postAs("xml"));
		System.out.println(myApi.putAs("xml"));
		System.out.println(myApi.deleteAs("xml"));
		System.out.println(myApi.headAs("json").get("X-MyJsonApi-Timestamp").get());
		System.out.println(myApi.optionsAs("json").get("Allow").get());

		System.out.println(myApi.getAs("json"));
		System.out.println(myApi.postAs("json"));
		System.out.println(myApi.putAs("json"));
		System.out.println(myApi.deleteAs("json"));
		System.out.println(myApi.headAs("xml").get("X-MyXmlApi-Timestamp").get());
		System.out.println(myApi.optionsAs("xml").get("Allow").get());

		MultipartParameters parameters = new MultipartParameters();
		parameters.put("destination", "/tmp/java-restify-api/");
		parameters.put(MultipartFile.create("file", "java-restify-file.jpg", ContentType.of("image/jpeg"),
				MyApiClient.class.getResourceAsStream("/file.jpg")));

		String newFile = myApi.upload(parameters);
		System.out.println("New file created on server " + newFile);

		EndpointResponse<MyApiResponse> responseObject = myApi.getResponseObjectAs("json");
		System.out.println("Response object: " + responseObject);

		EndpointCall<MyApiResponse> call = myApi.call("json");
		System.out.println("Response object: " + call.execute());
	}
}
