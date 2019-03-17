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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;

import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreaker;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadata;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.spring.configure.Restifyable;

@RunWith(SpringRunner.class)
public abstract class SpelOnCircuitBreakerMetadataResolverTest {

	@Autowired
	protected SpelOnCircuitBreakerMetadataResolver resolver;

	@SpringBootTest(classes = MyConfiguration.class)
	public static class BasicConfiguration extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("simple")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("MyApi"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("simple"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(false));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
					"restify.my-api.circuit-breaker.group-key=my-group-key",
					"restify.my-api.circuit-breaker.command-key=my-command-key",
					"restify.my-api.circuit-breaker.thread-pool-key=my-thread-pool-key"})
	public static class WithBasicPropertiesOnConfiguration extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("simple")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("my-group-key"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("my-command-key"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.threadPoolKey().get(), equalTo("my-thread-pool-key"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
					"restify.my-api.circuit-breaker.[MyApi].command-key=my-command-key",
					"restify.my-api.circuit-breaker.[MyApi].thread-pool-key=my-thread-pool-key"})
	public static class ByGroupKeyConfiguration extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("simple")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("MyApi"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("my-command-key"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.threadPoolKey().get(), equalTo("my-thread-pool-key"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
					"restify.my-api.circuit-breaker.properties.[circuit-breaker-whatever-property-1]=whatever-value-1",
					"restify.my-api.circuit-breaker.properties.[circuit-breaker-whatever-property-2]=whatever-value-2"})
	public static class WithProperties extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("simple")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("MyApi"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("simple"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(false));

			assertThat(onCircuitBreaker.properties().values(), hasSize(2));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit-breaker-whatever-property-1", "whatever-value-1"));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit-breaker-whatever-property-2", "whatever-value-2"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
					"restify.my-api.circuit-breaker.[MyApi].properties.[circuit-breaker-whatever-property-1]=whatever-value-1",
					"restify.my-api.circuit-breaker.[MyApi].properties.[circuit-breaker-whatever-property-2]=whatever-value-2"})
	public static class WithPropertiesByGroupKey extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("simple")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("MyApi"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("simple"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(false));

			assertThat(onCircuitBreaker.properties().values(), hasSize(2));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit-breaker-whatever-property-1", "whatever-value-1"));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit-breaker-whatever-property-2", "whatever-value-2"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
					"restify.my-api.circuit-breaker.[MyApi].[simple].properties.[circuit-breaker-whatever-property-1]=whatever-value-1",
					"restify.my-api.circuit-breaker.[MyApi].[simple].properties.[circuit-breaker-whatever-property-2]=whatever-value-2"})
	public static class WithPropertiesByCommandKey extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("simple")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("MyApi"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("simple"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(false));

			assertThat(onCircuitBreaker.properties().values(), hasSize(2));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit-breaker-whatever-property-1", "whatever-value-1"));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit-breaker-whatever-property-2", "whatever-value-2"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
					"restify.my-api.circuit-breaker.[my-group-key].command-key=my-command-key",
					"restify.my-api.circuit-breaker.[my-group-key].thread-pool-key=my-thread-pool-key"})
	public static class WithExplicitGroupKey extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("withGroupKey")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("my-group-key"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("my-command-key"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.threadPoolKey().get(), equalTo("my-thread-pool-key"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {"restify.my-api.circuit-breaker.[my-group-key].thread-pool-key=my-thread-pool-key"})
	public static class WithExplicitCommandKey extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("withCommandKey")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("my-group-key"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("my-command-key"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.threadPoolKey().get(), equalTo("my-thread-pool-key"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class)
	public static class WithExplicitThreadPoolCommandKey extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("withThreadPoolKey")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("my-group-key"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("my-command-key"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.threadPoolKey().get(), equalTo("my-thread-pool-key"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
					"myApi.hystrix.groupKey=my-api-group-key",
					"myApi.hystrix.commandKey=my-api-command-key",
					"myApi.hystrix.threadPoolKey=my-api-thread-pool-key"})
	public static class WithExplicitDynamicConfiguration extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("dynamic")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("my-api-group-key"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("my-api-command-key"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.threadPoolKey().get(), equalTo("my-api-thread-pool-key"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
					"hystrix.whatever.property.value=propertyValue",
					"hystrix.whatever.otherProperty.value=otherPropertyValue"})
	public static class WithExplicitDynamicProperties extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("dynamicProperties")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("MyApi"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("dynamicProperties"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(false));

			assertThat(onCircuitBreaker.properties().values(), hasSize(2));
			assertThat(onCircuitBreaker.properties(), hasEntry("hystrix.whatever.property", "propertyValue"));
			assertThat(onCircuitBreaker.properties(), hasEntry("hystrix.whatever.otherProperty", "otherPropertyValue"));
		}
	}

	@SpringBootTest(classes = MyConfiguration.class,
			properties = {
				"restify.my-api.circuit-breaker.properties.[circuit.breaker.some.property.1]=some-value-1",
				"restify.my-api.circuit-breaker.[MyApi].properties.[circuit.breaker.some.property.2]=some-value-2",
				"restify.my-api.circuit-breaker.[MyApi].[withProperties].properties.[circuit.breaker.some.property.3]=some-value-3"})
	public static class WithExplicitAndConfiguredProperties extends SpelOnCircuitBreakerMetadataResolverTest {

		@Test
		public void test() throws Exception {
			OnCircuitBreakerMetadata onCircuitBreaker = resolver.resolve(new SimpleEndpointMethod(MyApi.class.getMethod("withProperties")));

			assertThat(onCircuitBreaker.groupKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.groupKey().get(), equalTo("MyApi"));

			assertThat(onCircuitBreaker.commandKey().isPresent(), is(true));
			assertThat(onCircuitBreaker.commandKey().get(), equalTo("withProperties"));

			assertThat(onCircuitBreaker.threadPoolKey().isPresent(), is(false));

			assertThat(onCircuitBreaker.properties().values(), hasSize(5));
			assertThat(onCircuitBreaker.properties(), hasEntry("hystrix.whatever.property", "propertyValue"));
			assertThat(onCircuitBreaker.properties(), hasEntry("hystrix.whatever.otherProperty", "otherPropertyValue"));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit.breaker.some.property.1", "some-value-1"));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit.breaker.some.property.2", "some-value-2"));
			assertThat(onCircuitBreaker.properties(), hasEntry("circuit.breaker.some.property.3", "some-value-3"));
		}
	}

	@SpringBootApplication
	static class MyConfiguration {
	}

	@Restifyable(endpoint = "http://my-api")
	interface MyApi {

		@OnCircuitBreaker
		@GetMapping("/")
		String simple();

		@OnCircuitBreaker(groupKey = "my-group-key")
		@GetMapping("/")
		String withGroupKey();

		@OnCircuitBreaker(groupKey = "my-group-key", commandKey = "my-command-key")
		@GetMapping("/")
		String withCommandKey();

		@OnCircuitBreaker(groupKey = "my-group-key", commandKey = "my-command-key", threadPoolKey = "my-thread-pool-key")
		@GetMapping("/")
		String withThreadPoolKey();

		@OnCircuitBreaker(properties = {
			@CircuitBreakerProperty(name = "hystrix.whatever.property", value="propertyValue"),
			@CircuitBreakerProperty(name = "hystrix.whatever.otherProperty", value="otherPropertyValue")
		})
		@GetMapping("/")
		String withProperties();

		@OnCircuitBreaker(groupKey = "${myApi.hystrix.groupKey}", commandKey = "${myApi.hystrix.commandKey}", threadPoolKey = "${myApi.hystrix.threadPoolKey}")
		@GetMapping("/")
		String dynamic();

		@OnCircuitBreaker(properties = {
			@CircuitBreakerProperty(name = "hystrix.whatever.property", value="${hystrix.whatever.property.value}"),
			@CircuitBreakerProperty(name = "hystrix.whatever.otherProperty", value="${hystrix.whatever.otherProperty.value}")
		})
		@GetMapping("/")
		String dynamicProperties();
	}

	private class SimpleEndpointMethod extends EndpointMethod {

		public SimpleEndpointMethod(Method javaMethod) {
			super(javaMethod, "/", "GET");
		}
	}
}
