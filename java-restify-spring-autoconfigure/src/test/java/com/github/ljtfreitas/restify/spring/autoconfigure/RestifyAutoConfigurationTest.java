package com.github.ljtfreitas.restify.spring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.github.ljtfreitas.restify.http.client.call.handler.jsoup.JsoupDocumentEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.reactor.FluxEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.reactor.MonoEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava.RxJavaCompletableEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava.RxJavaObservableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava.RxJavaSingleEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2CompletableEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2FlowableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2MaybeEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2ObservableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2SingleEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.ArrayEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.EitherWithStringEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.EitherWithThrowableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.FutureEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.IndexedSeqEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.LazyEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.ListEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.OptionEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.QueueEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.SeqEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.SetEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.TraversableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.TryEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedFormObjectMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedMapMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedParametersMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormFileObjectMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormMapMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormObjectMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormParametersMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.GsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JacksonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonBMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonPMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetByteArrayMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetInputStreamMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetSerializableMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetStreamMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.ScalarMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextHtmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.JaxBXmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.XmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.jaxrs.contract.metadata.JaxRsContractReader;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.HttpHeadersEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.HttpStatusEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.ResponseEntityEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.AsyncResultEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.DeferredResultEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.ListenableFutureEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.ListenableFutureTaskEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.WebAsyncTaskEndpointCallHandlerAdapter;
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

			mockServerClient
				.when(request()
					.withMethod("GET")
					.withPath("/sample-customized")
					.withHeader("X-Customized", "true")
					.withHeader("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
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

		@Test
		public void shouldCreateBeanOfMyCustomizedApiType() {
			ApplicationContextRunner contextRunner = new ApplicationContextRunner()
					.withUserConfiguration(TestRestifyConfiguration.class)
					.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class))
					.withPropertyValues("restify.my-customized-api.endpoint:http://localhost:8080");

			contextRunner.run(context -> {
				MyCustomizedApi myCustomizedApi = context.getBean(MyCustomizedApi.class);

				assertEquals("It's works!", myCustomizedApi.sample());
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
				assertNotNull(context.getBean(HttpHeadersEndpointCallHandlerAdapter.class));
				assertNotNull(context.getBean(HttpStatusEndpointCallHandlerAdapter.class));
				assertNotNull(context.getBean(ResponseEntityEndpointCallHandlerFactory.class));
				assertNotNull(context.getBean(EndpointResponseErrorFallback.class));
			});
		}

		@Test
		public void shouldCreateWebFluxBeans() {
			contextRunner.run(context -> {
				assertNotNull(context.getBean(WebClientEndpointRequestExecutor.class));
				assertNotNull(context.getBean(reactor.core.scheduler.Scheduler.class));
				assertNotNull(context.getBean(FluxEndpointCallHandlerAdapter.class));
				assertNotNull(context.getBean(MonoEndpointCallHandlerAdapter.class));
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
				assertNotNull(context.getBean(AsyncResultEndpointCallHandlerAdapter.class));
				assertNotNull(context.getBean(DeferredResultEndpointCallHandlerAdapter.class));
				assertNotNull(context.getBean(ListenableFutureTaskEndpointCallHandlerAdapter.class));
				assertNotNull(context.getBean(ListenableFutureEndpointCallHandlerAdapter.class));
				assertNotNull(context.getBean(WebAsyncTaskEndpointCallHandlerAdapter.class));
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

	public abstract static class HttpMessageConvertersConfigurationTest {

		protected ApplicationContextRunner contextRunner;

		@Before
		public void setup() {
			contextRunner = new ApplicationContextRunner()
					.withUserConfiguration(TestRestifyConfiguration.class)
					.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class));
		}

		public static class JacksonJsonHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateJacksonMessageConverter() {
				contextRunner
					.withClassLoader(new FilteredClassLoader(GsonMessageConverter.class, JsonPMessageConverter.class, JsonBMessageConverter.class))
					.run(context -> {
						assertThat(context)
							.hasSingleBean(JacksonMessageConverter.class)
							.hasSingleBean(JsonMessageConverter.class)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(1);
					});
			}
		}

		public static class GsonJsonHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateGsonMessageConverter() {
				contextRunner
					.withClassLoader(new FilteredClassLoader(JacksonMessageConverter.class, JsonPMessageConverter.class, JsonBMessageConverter.class))
					.run(context -> {
						assertThat(context)
							.hasSingleBean(GsonMessageConverter.class)
							.hasSingleBean(JsonMessageConverter.class)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(1);
					});
			}
		}

		public static class JsonBJsonHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateJsonBMessageConverter() {
				contextRunner
					.withClassLoader(new FilteredClassLoader(JacksonMessageConverter.class, JsonPMessageConverter.class, GsonMessageConverter.class))
					.run(context -> {
						assertThat(context)
							.hasSingleBean(JsonBMessageConverter.class)
							.hasSingleBean(JsonMessageConverter.class)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(1);
					});
			}
		}

		public static class JsonPJsonHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateJsonPMessageConverter() {
				contextRunner
					.withClassLoader(new FilteredClassLoader(JacksonMessageConverter.class, GsonMessageConverter.class, JsonBMessageConverter.class))
					.run(context -> {
						assertThat(context)
							.hasSingleBean(JsonPMessageConverter.class)
							.hasSingleBean(JsonMessageConverter.class)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(1);
					});
			}
		}

		public static class JaxBXmlHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateJaxBMessageConverter() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(JaxBXmlMessageConverter.class)
							.hasSingleBean(XmlMessageConverter.class)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(1);
					});
			}
		}

		public static class FormUrlEncodedHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateFormUrlEncodedMessageConverters() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(FormURLEncodedFormObjectMessageConverter.class)
							.hasSingleBean(FormURLEncodedMapMessageConverter.class)
							.hasSingleBean(FormURLEncodedParametersMessageConverter.class);

						assertThat(context)
							.getBeans(FormURLEncodedMessageConverter.class)
								.size()
									.isEqualTo(3);

						assertThat(context)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(3);
					});
			}
		}

		public static class MultipartFormHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateMultipartFormMessageConverters() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(MultipartFormFileObjectMessageWriter.class)
							.hasSingleBean(MultipartFormMapMessageWriter.class)
							.hasSingleBean(MultipartFormObjectMessageWriter.class)
							.hasSingleBean(MultipartFormParametersMessageWriter.class);

						assertThat(context)
							.getBeans(MultipartFormMessageWriter.class)
								.size()
									.isEqualTo(4);

						assertThat(context)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(4);
					});
			}
		}

		public static class TextHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateTextMessageConverters() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(ScalarMessageConverter.class)
							.hasSingleBean(TextHtmlMessageConverter.class)
							.hasSingleBean(TextPlainMessageConverter.class);

						assertThat(context)
							.getBeans(TextMessageConverter.class)
								.size()
									.isEqualTo(3);

						assertThat(context)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(4);
					});
			}
		}

		public static class OctetStreamHttpMessageConvertersConfigurationTest extends HttpMessageConvertersConfigurationTest {

			@Test
			public void shouldCreateOctetStreamMessageConverters() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(OctetByteArrayMessageConverter.class)
							.hasSingleBean(OctetInputStreamMessageConverter.class)
							.hasSingleBean(OctetSerializableMessageConverter.class);

						assertThat(context)
							.getBeans(OctetStreamMessageConverter.class)
								.size()
									.isEqualTo(3);

						assertThat(context)
							.getBeans(HttpMessageConverter.class)
								.size()
									.isGreaterThanOrEqualTo(3);
					});
			}
		}
	}

	public abstract static class EndpointCallHandlersConfigurationTest {

		protected ApplicationContextRunner contextRunner;

		@Before
		public void setup() {
			contextRunner = new ApplicationContextRunner()
					.withUserConfiguration(TestRestifyConfiguration.class)
					.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class));
		}

		public static class GuavaEndpointCallHandlersConfigurationTest extends EndpointCallHandlersConfigurationTest {

			@Test
			public void shouldCreateGuavaEndpointCallHandlers() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(com.github.ljtfreitas.restify.http.client.call.handler.guava.OptionalEndpointCallHandlerFactory.class)
							.hasSingleBean(com.github.ljtfreitas.restify.http.client.call.handler.guava.ListenableFutureCallbackEndpointCallHandlerAdapter.class)
							.hasSingleBean(com.github.ljtfreitas.restify.http.client.call.handler.guava.ListenableFutureEndpointCallHandlerAdapter.class)
							.hasSingleBean(com.github.ljtfreitas.restify.http.client.call.handler.guava.ListenableFutureTaskEndpointCallHandlerAdapter.class);
					});
			}
		}

		public static class JsoupEndpointCallHandlersConfigurationTest extends EndpointCallHandlersConfigurationTest {

			@Test
			public void shouldCreateJsoupEndpointCallHandlers() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(JsoupDocumentEndpointCallHandlerFactory.class);
					});
			}
		}

		public static class RxJavaEndpointCallHandlersConfigurationTest extends EndpointCallHandlersConfigurationTest {

			@Test
			public void shouldCreateRxJavaEndpointCallHandlers() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(rx.Scheduler.class)
							.hasSingleBean(RxJavaCompletableEndpointCallHandlerFactory.class)
							.hasSingleBean(RxJavaObservableEndpointCallHandlerAdapter.class)
							.hasSingleBean(RxJavaSingleEndpointCallHandlerAdapter.class);
					});
			}
		}

		public static class RxJava2EndpointCallHandlersConfigurationTest extends EndpointCallHandlersConfigurationTest {

			@Test
			public void shouldCreateRxJava2EndpointCallHandlers() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(io.reactivex.Scheduler.class)
							.hasSingleBean(RxJava2CompletableEndpointCallHandlerFactory.class)
							.hasSingleBean(RxJava2FlowableEndpointCallHandlerAdapter.class)
							.hasSingleBean(RxJava2MaybeEndpointCallHandlerAdapter.class)
							.hasSingleBean(RxJava2ObservableEndpointCallHandlerAdapter.class)
							.hasSingleBean(RxJava2SingleEndpointCallHandlerAdapter.class);
					});
			}
		}

		public static class VavrEndpointCallHandlersConfigurationTest extends EndpointCallHandlersConfigurationTest {

			@Test
			public void shouldCreateVavrEndpointCallHandlers() {
				contextRunner
					.run(context -> {
						assertThat(context)
							.hasSingleBean(ArrayEndpointCallHandlerAdapter.class)
							.hasSingleBean(EitherWithStringEndpointCallHandlerAdapter.class)
							.hasSingleBean(EitherWithThrowableEndpointCallHandlerAdapter.class)
							.hasSingleBean(FutureEndpointCallHandlerAdapter.class)
							.hasSingleBean(IndexedSeqEndpointCallHandlerAdapter.class)
							.hasSingleBean(LazyEndpointCallHandlerAdapter.class)
							.hasSingleBean(ListEndpointCallHandlerAdapter.class)
							.hasSingleBean(OptionEndpointCallHandlerFactory.class)
							.hasSingleBean(QueueEndpointCallHandlerAdapter.class)
							.hasSingleBean(SeqEndpointCallHandlerAdapter.class)
							.hasSingleBean(SetEndpointCallHandlerAdapter.class)
							.hasSingleBean(TraversableEndpointCallHandlerAdapter.class)
							.hasSingleBean(TryEndpointCallHandlerAdapter.class);
					});
			}
		}
	}

	@Configuration
	@Import({MyCustomizedApiConfiguration.class, TestRestifyConfigurationRegistrar.class})
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
