/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.spring.configure;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractReader;
import com.github.ljtfreitas.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.spring.contract.metadata.SpelDynamicParameterExpressionResolver;
import com.github.ljtfreitas.restify.http.spring.contract.metadata.SpringWebContractReader;

@Configuration
public class RestifySpringWebConfiguration {

	@Autowired(required = false)
	private RestTemplateBuilder restTemplateBuilder;

	@Autowired(required = false)
	private RestOperations restTemplate;

	@ConditionalOnMissingBean
	@Bean
	public EndpointRequestExecutor endpointRequestExecutor(EndpointResponseErrorFallback endpointResponseErrorFallback) {
		RestOperations restOperations = Optional.ofNullable(restTemplate)
				.orElseGet(() -> Optional.ofNullable(restTemplateBuilder)
					.map(builder -> builder.build())
						.orElseGet(() -> new RestTemplate()));

		return new RestOperationsEndpointRequestExecutor(restOperations, endpointResponseErrorFallback);
	}

	@Configuration
	@ConditionalOnProperty(name = "restify.contract", havingValue = "spring-web", matchIfMissing = true)
	public static class RestifySpringWebContractConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public ContractReader restifyContractReader(ContractExpressionResolver expressionResolver) {
			return new SpringWebContractReader(expressionResolver);
		}

		@ConditionalOnMissingBean
		@Bean
		public ContractExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
			return new SpelDynamicParameterExpressionResolver(beanFactory);
		}
	}
}