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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.ljtfreitas.restify.http.client.apache.httpclient.ApacheHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.okhttp.OkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.HttpHeadersEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.ResponseEntityEndpointCallExecutableFactory;

@Configuration
public class RestifyDefaultConfiguration {

	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "restify.http.client", havingValue = "jdk", matchIfMissing = true)
	@Bean
	public HttpClientRequestFactory jdkHttpClientRequestFactory() {
		return new JdkHttpClientRequestFactory();
	}

	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "restify.http.client", havingValue = "http-client")
	@Bean
	public HttpClientRequestFactory apacheHttpClientRequestFactory() {
		return new ApacheHttpClientRequestFactory();
	}

	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "restify.http.client", havingValue = "ok-http")
	@Bean
	public HttpClientRequestFactory okHttpClientRequestFactory() {
		return new OkHttpClientRequestFactory();
	}

	@ConditionalOnMissingBean
	@Bean
	public HttpHeadersEndpointCallExecutableFactory httpHeadersEndpointCallExecutableFactory() {
		return new HttpHeadersEndpointCallExecutableFactory();
	}

	@ConditionalOnMissingBean
	@Bean
	public ResponseEntityEndpointCallExecutableFactory<Object> responseEntityEndpointCallExecutableFactory() {
		return new ResponseEntityEndpointCallExecutableFactory<>();
	}

	@ConditionalOnMissingBean
	@Bean
	public EndpointResponseErrorFallback endpointResponseErrorFallback(RestifyConfigurationProperties properties) {
		return properties.getError().isEmptyOnNotFound() ?
				DefaultEndpointResponseErrorFallback.emptyOnNotFound() : new DefaultEndpointResponseErrorFallback();
	}

}
