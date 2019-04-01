package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadata;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.observers.TestSubscriber;

@RunWith(MockitoJUnitRunner.class)
public class HystrixObservableCommandEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	@Mock
	private OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver;

	@Mock
	private OnCircuitBreakerMetadata onCircuitBreakerMetadata;

	@InjectMocks
	private HystrixObservableCommandEndpointCallHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		when(delegate.handle(any(), any()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));

		when(onCircuitBreakerMetadataResolver.resolve(any()))
			.thenReturn(onCircuitBreakerMetadata);

		when(onCircuitBreakerMetadata.groupKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.commandKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.threadPoolKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.properties()).thenReturn(Collections.emptyMap());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsHystrixObservableCommand() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("command"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotHystrixObservableCommand() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateHandlerFromEndpointMethodWithHystrixObservableCommandReturnType() throws Exception {
		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		String expected = "result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(expected));

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncEndpointCall, null);

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals(expected, result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), any());
	}

	@Test
	public void shouldReturnObjectTypeWhenHystrixObservableCommandIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbCommand"))));
	}

	@Test
	public void shouldPropagateExceptionWhenHystrixObservableCommandThrowsException() throws Exception {
		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		RuntimeException exception = new RuntimeException("oooh!");

		CompletableFuture<String> futureAsException = new CompletableFuture<>();
		futureAsException.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(futureAsException);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncEndpointCall, null);

		TestSubscriber<String> subscriber = new TestSubscriber<>();
		hystrixCommand.toObservable().subscribe(subscriber);

		subscriber.awaitTerminalEvent();

		assertFalse(subscriber.getOnErrorEvents().isEmpty());

		subscriber.assertTerminalEvent();
	}

	interface SomeType {

		HystrixObservableCommand<String> command();

		@SuppressWarnings("rawtypes")
		HystrixObservableCommand dumbCommand();

		String string();
	}
}
