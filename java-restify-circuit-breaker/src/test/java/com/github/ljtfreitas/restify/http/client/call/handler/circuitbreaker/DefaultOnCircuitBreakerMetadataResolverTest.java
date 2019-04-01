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
package com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker;

import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_STRATEGY;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_CORE_SIZE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;

public class DefaultOnCircuitBreakerMetadataResolverTest {

	private SimpleOnCircuitBreakerMetadataResolver resolver;

	@Before
	public void setup() {
		resolver = new SimpleOnCircuitBreakerMetadataResolver();
	}

	@Test
	public void shouldBuildOnCircuitBreakerMetadataWhenAnnotationThereIsNotParameters() throws Exception {
		OnCircuitBreakerMetadata metadata = resolver.resolve(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class
				.getMethod("simple")));

		assertThat(metadata.groupKey().isPresent(), is(false));
		assertThat(metadata.commandKey().isPresent(), is(false));
		assertThat(metadata.threadPoolKey().isPresent(), is(false));
	}

	@Test
	public void shouldBuildOnCircuitBreakerMetadataFromAnnotationParameters() throws Exception {
		OnCircuitBreakerMetadata metadata = resolver.resolve(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class
				.getMethod("customizedKeys")));

		assertThat(metadata.groupKey().get(), equalTo("myGroupKey"));
		assertThat(metadata.commandKey().get(), equalTo("myTestCommandKey"));
		assertThat(metadata.threadPoolKey().get(), equalTo("myTestThreadPoolKey"));
	}

	@Test
	public void shouldBuildOnCircuitBreakerMetadataPropertiesFromMethodAnnotation() throws Exception {
		OnCircuitBreakerMetadata metadata = resolver.resolve(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class
				.getMethod("customizedProperties")));

		assertThat(metadata.properties(), hasEntry(EXECUTION_ISOLATION_STRATEGY, "SEMAPHORE"));
		assertThat(metadata.properties(), hasEntry(EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, "2500"));
		assertThat(metadata.properties(), hasEntry(THREAD_POOL_CORE_SIZE, "50"));
	}

	interface MyApiWithCircuitBreaker {

		@OnCircuitBreaker
		String simple();

		@OnCircuitBreaker(groupKey = "myGroupKey", commandKey = "myTestCommandKey", threadPoolKey = "myTestThreadPoolKey")
		String customizedKeys();

		@OnCircuitBreaker(properties = {
				@CircuitBreakerProperty(name = EXECUTION_ISOLATION_STRATEGY, value = "SEMAPHORE"),
				@CircuitBreakerProperty(name = EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "2500"),
				@CircuitBreakerProperty(name = THREAD_POOL_CORE_SIZE, value = "50")})
		String customizedProperties();
	}

	class SimpleEndpointMethod extends EndpointMethod {

		public SimpleEndpointMethod(Method javaMethod) {
			super(javaMethod, "/", "GET");
		}
	}
}
