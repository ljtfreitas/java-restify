package com.github.ljtfreitas.restify.http.client.call.handler.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class MonoEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private MonoEndpointCallHandlerAdapter<String, String> adapter;

	private String asyncResult;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		adapter = new MonoEndpointCallHandlerAdapter<>();

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		asyncResult = "mono result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(asyncResult));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsMono() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("mono"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotMono() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArOgumentTypeOfMono() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("mono"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenMonoIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbMono"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithMonoReturnType() throws Exception {
		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		Mono<String> mono = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(mono);

		StepVerifier.create(mono)
			.expectNext(asyncResult)
			.expectComplete()
			.verify();
	}

	@Test
	public void shouldSubscribeErrorOnSingleWhenCreatedHandlerWithRxJava2SingleReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Mono<String> mono = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(mono);

		StepVerifier.create(mono)
			.expectError()
			.verify();
	}

	@Test
	public void shouldCreateSyncHandlerFromEndpointMethodWithMonoReturnType() throws Exception {
		EndpointCallHandler<Mono<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		when(asyncEndpointCall.execute())
			.thenReturn(asyncResult);

		Mono<String> mono = handler.handle(asyncEndpointCall, null);

		assertNotNull(mono);

		StepVerifier.create(mono)
			.expectNext(asyncResult)
			.expectComplete()
			.verify();
	}

	interface SomeType {

		Mono<String> mono();

		@SuppressWarnings("rawtypes")
		Mono dumbMono();

		String string();
	}
}
