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
package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfiguration;

@Configuration
@AutoConfigureBefore(RestifyAutoConfiguration.class)
public class RestifyHystrixAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public HystrixCommandFallbackEndpointCallExecutableAdapter hystrixCommandFallbackEndpointCallExecutableFactory(
			HystrixFallbackRegistry fallbackObjectFactory) {
		return new HystrixCommandFallbackEndpointCallExecutableAdapter(fallbackObjectFactory);
	}

	@ConditionalOnMissingBean
	@Bean
	public HystrixCircuitBreakerFallbackEndpointCallExecutableAdapter hystrixCircuitBreakerFallbackEndpointCallExecutableFactory(
			HystrixFallbackRegistry fallbackObjectFactory) {
		return new HystrixCircuitBreakerFallbackEndpointCallExecutableAdapter(fallbackObjectFactory);
	}

	@Configuration
	protected static class RestifyHystrixFallbackConfiguration implements BeanFactoryAware {

		private BeanFactory beanFactory;

		@Bean
		public HystrixFallbackRegistry restifyFallbackObjectFactory() {
			return new HystrixFallbackRegistry(beanFactory);
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}
	}
}
