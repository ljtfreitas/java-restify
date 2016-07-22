package com.restify.sample.client;

import com.restify.http.RestifyProxyBuilder;

public class MyApiClient {

	public static void main(String[] args) {
		MyApi myApi = new RestifyProxyBuilder()
				.target(MyApi.class, "http://localhost:8080")
				.build();

		System.out.println(myApi.getAsJson());
		System.out.println(myApi.postAsJson());
		System.out.println(myApi.putAsJson());
		System.out.println(myApi.deleteAsJson());

		System.out.println(myApi.getAsXml());
		System.out.println(myApi.postAsXml());
		System.out.println(myApi.putAsXml());
		System.out.println(myApi.deleteAsXml());

	}


}
