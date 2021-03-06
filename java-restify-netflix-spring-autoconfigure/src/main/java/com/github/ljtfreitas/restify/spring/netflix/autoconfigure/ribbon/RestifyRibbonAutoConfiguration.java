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
package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancerAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.spring.client.request.async.WebClientEndpointRequestExecutor;
import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfiguration;
import com.netflix.loadbalancer.ILoadBalancer;

@Configuration
@ConditionalOnProperty(value = "restify.ribbon.enabled")
@ConditionalOnClass(ILoadBalancer.class)
@ConditionalOnBean(LoadBalancerClient.class)
@AutoConfigureBefore(RestifyAutoConfiguration.class)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
public class RestifyRibbonAutoConfiguration {

	@Configuration
	@ConditionalOnMissingClass("org.springframework.web.reactive.function.client.WebClient")
	static class LoadBalancedClientConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public EndpointRequestExecutor loadBalancedEndpointRequestExecutor(@LoadBalanced RestTemplate restTemplate) {
			return new RestOperationsEndpointRequestExecutor(restTemplate);
		}

		@Conditional(LoadBalancedRestTemplateCondition.class)
		@Bean @LoadBalanced
		public RestTemplate loadBalancedRestTemplate() {
			return new RestTemplate();
		}
		
		static class LoadBalancedRestTemplateCondition extends AllNestedConditions {

			LoadBalancedRestTemplateCondition() {
				super(ConfigurationPhase.REGISTER_BEAN);
			}

			@ConditionalOnMissingBean(annotation = LoadBalanced.class)
			static class LoadBalancedQualifiedBeanMissing {
			}
		}
	}

	@Configuration
	@ConditionalOnClass(WebClient.class)
	@AutoConfigureBefore(ReactiveLoadBalancerAutoConfiguration.class)
	static class ReactiveLoadBalancedClientConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public EndpointRequestExecutor loadBalancedEndpointRequestExecutor(@LoadBalanced WebClient.Builder webClientBuilder) {
			return new WebClientEndpointRequestExecutor(webClientBuilder);
		}

		@Conditional(LoadBalancedWebClientCondition.class)
		@Bean @LoadBalanced
		public WebClient.Builder loadBalancedWebClientBuilder() {
			return WebClient.builder();
		}

		static class LoadBalancedWebClientCondition extends AllNestedConditions {
			
			LoadBalancedWebClientCondition() {
				super(ConfigurationPhase.REGISTER_BEAN);
			}
			
			@ConditionalOnMissingBean(annotation = LoadBalanced.class)
			static class LoadBalancedQualifiedBeanMissing {
			}
		}
	}	
}
