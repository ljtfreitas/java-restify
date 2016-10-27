package com.restify.spring.configure;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.HttpClientRequestFactory;
import com.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.restify.http.contract.metadata.RestifyContractReader;
import com.restify.http.spring.client.call.exec.HttpHeadersEndpointCallExecutableFactory;
import com.restify.http.spring.client.call.exec.ResponseEntityEndpointCallExecutableFactory;
import com.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;
import com.restify.http.spring.contract.SpringMvcContractReader;
import com.restify.http.spring.contract.metadata.SpelDynamicParameterExpressionResolver;
import com.restify.http.spring.contract.metadata.SpringDynamicParameterExpressionResolver;

@Configuration
class RestifyConfiguration {

	@Bean
	public EndpointRequestExecutor endpointRequestExecutor(ObjectProvider<RestTemplate> restTemplate) {
		return new RestOperationsEndpointRequestExecutor(
				Optional.ofNullable(restTemplate.getIfAvailable()).orElseGet(() -> new RestTemplate()));
	}

	@Bean
	public RestifyContractReader restifyContractReader(SpringDynamicParameterExpressionResolver expressionResolver) {
		return new SpringMvcContractReader(expressionResolver);
	}

	@Bean
	public SpringDynamicParameterExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
		return new SpelDynamicParameterExpressionResolver(beanFactory);
	}

	@Bean
	public HttpClientRequestFactory httpClientRequestFactory() {
		return new JdkHttpClientRequestFactory();
	}

	@Bean
	public HttpHeadersEndpointCallExecutableFactory httpHeadersEndpointCallExecutableFactory() {
		return new HttpHeadersEndpointCallExecutableFactory();
	}

	@Bean
	public ResponseEntityEndpointCallExecutableFactory<Object> responseEntityEndpointCallExecutableFactory() {
		return new ResponseEntityEndpointCallExecutableFactory<>();
	}
}
