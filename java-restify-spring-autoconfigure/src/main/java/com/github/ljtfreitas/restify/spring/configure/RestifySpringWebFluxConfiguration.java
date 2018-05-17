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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.FluxEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.MonoEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.request.async.WebClientEndpointRequestExecutor;

@Configuration
@ConditionalOnClass(WebClient.class)
public class RestifySpringWebFluxConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public EndpointRequestExecutor webClientEndpointRequestExecutor(EndpointResponseErrorFallback endpointResponseErrorFallback,
			ObjectProvider<WebClient> webClientProvider) {

		WebClient webClient = webClientProvider.getIfAvailable();

		return webClient == null ? new WebClientEndpointRequestExecutor() : new WebClientEndpointRequestExecutor(webClient);
	}

	@ConditionalOnMissingBean
	@Bean
	public FluxEndpointCallExecutableFactory<Object, Object> fluxEndpointCallExecutableFactory() {
		return new FluxEndpointCallExecutableFactory<>();
	}

	@ConditionalOnMissingBean
	@Bean
	public MonoEndpointCallExecutableFactory<Object, Object> monoEndpointCallExecutableFactory() {
		return new MonoEndpointCallExecutableFactory<>();
	}
}