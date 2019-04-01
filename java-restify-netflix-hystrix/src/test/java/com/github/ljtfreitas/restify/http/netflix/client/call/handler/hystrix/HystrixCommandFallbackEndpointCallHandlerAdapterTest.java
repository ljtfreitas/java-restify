package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.Fallback;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadata;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.WithFallback;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class HystrixCommandFallbackEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private EndpointCallHandler<Optional<String>, String> delegateToOptional;

	@Mock
	private OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver;

	@Mock
	private OnCircuitBreakerMetadata onCircuitBreakerMetadata;

	private HystrixCommandEndpointCallHandlerAdapter<String, String> adapter;

	private HystrixCommandEndpointCallHandlerAdapter<Optional<String>, String> adapterToOptional;

	@Spy
	private FallbackOfSameType fallback = new FallbackOfSameType();

	@Spy
	private FallbackOfOtherType fallbackOfOtherType = new FallbackOfOtherType();

	private RuntimeException exception;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(Fallback.of(fallback), onCircuitBreakerMetadataResolver);

		adapterToOptional = new HystrixCommandEndpointCallHandlerAdapter<>(Fallback.of(fallback), onCircuitBreakerMetadataResolver);

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegateToOptional.handle(notNull(EndpointCall.class), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));

		exception = new RuntimeException("oooh!");

		when(onCircuitBreakerMetadataResolver.resolve(any()))
			.thenReturn(onCircuitBreakerMetadata);

		when(onCircuitBreakerMetadata.groupKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.commandKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.threadPoolKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.properties()).thenReturn(Collections.emptyMap());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).command();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnSimpleFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("string")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("string fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).string();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFutureFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("future fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).future();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnObservableFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("observable fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).observable();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromMethodWithArgsWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, new Object[] {arg});

		String result = hystrixCommand.execute();

		assertEquals("fallback with arg " + arg, result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).args(arg);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromMethodWithoutArgsWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("withoutArgs")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, new Object[0]);

		String result = hystrixCommand.execute();

		assertEquals("fallback without args", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).withoutArgs();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnValueFromFallbackOfOtherTypeWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("fallback to exception: " + exception.getMessage(), result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallbackOfOtherType).command(exception);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnValueFromFallbackOfOtherTypeUsingThrowableAsArgumentWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("fallback to exception: oooh!", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallbackOfOtherType).command(exception);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFutureValueFromFallbackOfOtherTypeWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("future fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallbackOfOtherType).future();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromMethodWithArgsFromFallbackOfOtherTypeWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType), onCircuitBreakerMetadataResolver);

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, new Object[] {arg});

		String result = hystrixCommand.execute();

		assertEquals("fallback with arg " + arg + " and exception: " + exception.getMessage(), result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallbackOfOtherType).args(arg, exception);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromMethodWithoutArgsFromFallbackOfOtherTypeWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("withoutArgs")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, new Object[0]);

		String result = hystrixCommand.execute();

		assertEquals("fallback without args", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallbackOfOtherType).withoutArgs();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromWithFallbackAnnotationWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(onCircuitBreakerMetadataResolver);

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(OtherType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("fallback to exception: " + exception.getMessage(), result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromMethodPresentOnWithFallbackAnnotationWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(onCircuitBreakerMetadataResolver);

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(OtherType.class.getMethod("otherCommand")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		String result = hystrixCommand.execute();

		assertEquals("whatever fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldExecuteFallbackValueWhenHystrixCommandExecuionThrowExceptionOnMethodWithReturnTypeDifferentOfHandledType() throws Exception {

		EndpointCallHandler<HystrixCommand<Optional<String>>, String> handler = adapterToOptional
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("optional")), delegateToOptional);

		HystrixCommand<Optional<String>> hystrixCommand = handler.handle(() -> {throw exception;}, null);

		Optional<String> result = hystrixCommand.execute();

		assertTrue(result.isPresent());
		assertEquals("optional fallback", result.get());

		verify(delegateToOptional).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).optional();		
	}

	interface SomeType {

		HystrixCommand<String> command();

		@SuppressWarnings("rawtypes")
		HystrixCommand dumbCommand();

		String string();
		
		Optional<String> optional();

		Future<String> future();

		Observable<String> observable();

		String args(String arg);

		String withoutArgs();
	}

	@WithFallback(FallbackOfOtherType.class)
	interface OtherType {

		String command();

		@WithFallback(value = FallbackOfOtherType.class, method = "whatever")
		String otherCommand();
	}

	static class FallbackOfSameType implements SomeType {

		@Override
		public HystrixCommand<String> command() {
			return new HystrixCommand<String>(HystrixCommandGroupKey.Factory.asKey("fallback")) {
				@Override
				protected String run() throws Exception {
					return "fallback";
				}
			};
		}

		@SuppressWarnings("rawtypes")
		@Override
		public HystrixCommand dumbCommand() {
			return new HystrixCommand(HystrixCommandGroupKey.Factory.asKey("fallback")) {
				@Override
				protected Object run() throws Exception {
					return "fallback";
				}
			};
		}

		@Override
		public String string() {
			return "string fallback";
		}

		@Override
		public Optional<String> optional() {
			return Optional.of("optional fallback");
		}
		
		@Override
		public Future<String> future() {
			return CompletableFuture.completedFuture("future fallback");
		}

		@Override
		public Observable<String> observable() {
			return Observable.just("observable fallback");
		}

		@Override
		public String args(String arg) {
			return "fallback with arg " + arg;
		}

		@Override
		public String withoutArgs() {
			return "fallback without args";
		}
	}

	static class FallbackOfOtherType {

		public String command(Throwable t) {
			return "fallback to exception: " + t.getMessage();
		}

		public String future() {
			return "future fallback";
		}

		public String args(String arg, Throwable t) {
			return "fallback with arg " + arg + " and exception: " + t.getMessage();
		}

		public String withoutArgs() {
			return "fallback without args";
		}

		public String whatever() {
			return "whatever fallback";
		}
	}
}
