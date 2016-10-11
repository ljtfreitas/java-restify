package com.restify.spring.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.restify.http.client.request.HttpClientRequestFactory;
import com.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.restify.http.contract.metadata.DefaultRestifyContractReader;
import com.restify.http.contract.metadata.RestifyContractReader;

@Configuration
class RestifyConfiguration {

	@Bean
	public RestifyContractReader restifyContractReader() {
		return new DefaultRestifyContractReader();
	}

	@Bean
	public HttpClientRequestFactory httpClientRequestFactory() {
		return new JdkHttpClientRequestFactory();
	}
}
