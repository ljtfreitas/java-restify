package com.github.ljtfreitas.restify.spring.autoconfigure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.ljtfreitas.restify.http.client.call.exec.reactor.FluxEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.call.exec.reactor.MonoEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.jaxrs.contract.metadata.JaxRsContractReader;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.HttpHeadersEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.HttpStatusEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.ResponseEntityEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.AsyncResultEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.DeferredResultEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.ListenableFutureEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.ListenableFutureTaskEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.WebAsyncTaskEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.spring.client.request.async.WebClientEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.spring.contract.metadata.SpelDynamicParameterExpressionResolver;
import com.github.ljtfreitas.restify.http.spring.contract.metadata.SpringWebContractReader;
import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfigurationTest.TestRestifyConfiguration.TestRestifyConfigurationRegistrar;
import com.github.ljtfreitas.restify.spring.configure.Async;

public class RestifyAutoConfigurationTest {

	public static class BasicAutoConfigurationTest {
		@Rule
		public MockServerRule mockServerRule = new MockServerRule(this, 8080);

		private MockServerClient mockServerClient;

		@Before
		public void setup() {
			mockServerClient = new MockServerClient("localhost", 8080);

			mockServerClient
				.when(request()
						.withMethod("GET")
						.withPath("/sample"))
				.respond(response()
						.withStatusCode(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("It's works!"));
		}

		@Test
		public void shouldCreateBeanOfMyApiType() {
			ApplicationContextRunner contextRunner = new ApplicationContextRunner()
					.withUserConfiguration(TestRestifyConfiguration.class)
					.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class))
					.withPropertyValues("restify.my-api.endpoint:http://localhost:8080");

			contextRunner.run(context -> {
				MyApi myApi = context.getBean(MyApi.class);

				assertEquals("It's works!", myApi.sample());
			});
		}
	}

	public static class BeanCreationConfigurationTest {

		private ApplicationContextRunner contextRunner;

		@Before
		public void setup() {
			contextRunner = new ApplicationContextRunner()
					.withUserConfiguration(TestRestifyConfiguration.class)
					.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class));
		}

		@Test
		public void shouldCreateDefaultBeans() {
			contextRunner.run(context -> {
				assertNotNull(context.getBean(HttpHeadersEndpointCallExecutableFactory.class));
				assertNotNull(context.getBean(HttpStatusEndpointCallExecutableFactory.class));
				assertNotNull(context.getBean(ResponseEntityEndpointCallExecutableFactory.class));
				assertNotNull(context.getBean(EndpointResponseErrorFallback.class));
			});
		}

		@Test
		public void shouldCreateWebFluxBeans() {
			contextRunner.run(context -> {
				assertNotNull(context.getBean(WebClientEndpointRequestExecutor.class));
				assertNotNull(context.getBean(FluxEndpointCallExecutableFactory.class));
				assertNotNull(context.getBean(MonoEndpointCallExecutableFactory.class));
			});
		}

		@Test
		public void shouldCreateWebBeans() {
			contextRunner.withClassLoader(new FilteredClassLoader(WebClient.class))
				.run(context -> {
					assertNotNull(context.getBean(RestOperationsEndpointRequestExecutor.class));
					assertNotNull(context.getBean(SpringWebContractReader.class));
					assertNotNull(context.getBean(SpelDynamicParameterExpressionResolver.class));
				});
		}
	}

	public static class AsyncConfigurationTest {

		private ApplicationContextRunner contextRunner;

		@Before
		public void setup() {
			contextRunner = new ApplicationContextRunner()
					.withUserConfiguration(TestRestifyConfiguration.class)
					.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class));
		}

		@Test
		public void shouldCreateAsyncBeans() {
			contextRunner.run(context -> {
				assertNotNull(context.getBean(AsyncListenableTaskExecutor.class));
				assertNotNull(context.getBean(ExecutorService.class));
				assertNotNull(context.getBean(AsyncResultEndpointCallExecutableFactory.class));
				assertNotNull(context.getBean(DeferredResultEndpointCallExecutableFactory.class));
				assertNotNull(context.getBean(ListenableFutureEndpointCallExecutableFactory.class));
				assertNotNull(context.getBean(ListenableFutureTaskEndpointCallExecutableFactory.class));
				assertNotNull(context.getBean(WebAsyncTaskEndpointCallExecutableFactory.class));
			});
		}

		@Test
		public void shouldCreateAsyncBeansWithCustomExecutor() {
			contextRunner.withUserConfiguration(TestRestifyAsyncConfiguration.class)
				.run(context -> {
					SimpleAsyncTaskExecutor taskExecutor = context.getBean(SimpleAsyncTaskExecutor.class);
					assertNotNull(taskExecutor);
					assertEquals("TestRestifyAsyncTaskExecutor", taskExecutor.getThreadNamePrefix());
			});
		}
	}

	public static class ContractConfigurationTest {

		private ApplicationContextRunner contextRunner;

		@Before
		public void setup() {
			contextRunner = new ApplicationContextRunner()
					.withUserConfiguration(TestRestifyConfiguration.class)
					.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class));
		}

		@Test
		public void shouldCreateSpringWebContractBean() {
			contextRunner.run(context -> {
				assertNotNull(context.getBean(SpringWebContractReader.class));
			});
		}

		@Test
		public void shouldCreateJaxRsContractBean() {
			contextRunner.withPropertyValues("restify.contract=jax-rs")
				.run(context -> {
					assertNotNull(context.getBean(JaxRsContractReader.class));
				});
		}
	}

	@Configuration
	@Import(TestRestifyConfigurationRegistrar.class)
	static class TestRestifyConfiguration {

		static class TestRestifyConfigurationRegistrar implements ImportBeanDefinitionRegistrar {
			@Override
			public void registerBeanDefinitions(AnnotationMetadata arg0, BeanDefinitionRegistry registry) {
				AutoConfigurationPackages.register(registry, RestifyAutoConfigurationTest.class.getPackage().getName());
			}
		}
	}

	@Configuration
	static class TestRestifyAsyncConfiguration {

		@Bean @Async
		public AsyncListenableTaskExecutor testAsyncTaskExecutor() {
			return new SimpleAsyncTaskExecutor("TestRestifyAsyncTaskExecutor");
		}

		@Bean @Async
		public ExecutorService testAsyncExecutorService(@Async AsyncTaskExecutor executor) {
			return new ExecutorServiceAdapter(executor);
		}
	}
}
