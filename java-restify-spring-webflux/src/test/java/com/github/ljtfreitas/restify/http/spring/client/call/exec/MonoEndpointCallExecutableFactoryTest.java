package com.github.ljtfreitas.restify.http.spring.client.call.exec;

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
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class MonoEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private MonoEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	private String asyncResult;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new MonoEndpointCallExecutableFactory<>(scheduler);

		when(delegate.execute(notNull(EndpointCall.class), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		asyncResult = "mono result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(asyncResult));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsMono() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("mono"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotMono() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArOgumentTypeOfMono() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("mono"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenMonoIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbMono"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithMonoReturnType() throws Exception {
		AsyncEndpointCallExecutable<Mono<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		Mono<String> mono = executable.executeAsync(asyncEndpointCall, null);

		assertNotNull(mono);

		StepVerifier.create(mono)
			.expectNext(asyncResult)
			.expectComplete()
			.verify();
	}

	@Test
	public void shouldSubscribeErrorOnSingleWhenCreatedExecutableWithRxJava2SingleReturnTypeThrowException() throws Exception {
		AsyncEndpointCallExecutable<Mono<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Mono<String> mono = executable.executeAsync(asyncEndpointCall, null);

		assertNotNull(mono);

		StepVerifier.create(mono)
			.expectError()
			.verify();
	}

	interface SomeType {

		Mono<String> mono();

		@SuppressWarnings("rawtypes")
		Mono dumbMono();

		String string();
	}
}
