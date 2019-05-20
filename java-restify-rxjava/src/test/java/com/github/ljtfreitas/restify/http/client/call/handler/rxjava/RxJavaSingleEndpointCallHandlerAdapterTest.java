package com.github.ljtfreitas.restify.http.client.call.handler.rxjava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
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

import rx.Single;
import rx.observers.AssertableSubscriber;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJavaSingleEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	@Mock
	private EndpointCall<String> endpointCall;

	private RxJavaSingleEndpointCallHandlerAdapter<String, String> adapter;

    private CompletableFuture<String> resultAsFuture;

    private String result;

	@Before
	public void setup() {
		adapter = new RxJavaSingleEndpointCallHandlerAdapter<>(Schedulers.immediate());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));

		result = "single result";

		when(endpointCall.execute())
		    .thenReturn(result);

		resultAsFuture = CompletableFuture.completedFuture(result);

		when(asyncEndpointCall.executeAsync())
		    .thenReturn(resultAsFuture);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJavaSingle() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJavaSingle() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}


	@Test
	public void shouldReturnArgumentTypeOfRxJavSingle() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJavaSingleIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbSingle"))));
	}

	@Test
	public void shouldGetSingleWithResponseOfCall() throws Exception {
		AsyncEndpointCallHandler<Single<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		when(delegate.handle(any(), anyVararg()))
			.thenReturn(resultAsFuture.join());

		Single<String> single = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(single);

		AssertableSubscriber<String> subscriber = single.test();

		subscriber.assertCompleted()
				  .assertNoErrors()
				  .assertValue(result);
	}

	@Test
	public void shouldSingleWithErrorWhenEndpointcallThrowsException() throws Exception {
		AsyncEndpointCallHandler<Single<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> resultAsFuture = new CompletableFuture<>();
		resultAsFuture.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(resultAsFuture);

		Single<String> single = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(single);

		AssertableSubscriber<String> subscriber = single.test();

		subscriber.assertError(exception);
	}

	@Test
	public void shouldGetSingleWithResponseOfSyncCall() throws Exception {
		EndpointCallHandler<Single<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		String result = "single result";

		when(endpointCall.execute())
			.thenReturn(result);

		when(delegate.handle(any(), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Single<String> single = handler.handle(endpointCall, null);

		assertNotNull(single);

		AssertableSubscriber<String> subscriber = single.test();

		subscriber.assertCompleted()
				  .assertNoErrors()
				  .assertValue(result);
	}

	interface SomeType {

		Single<String> single();

		@SuppressWarnings("rawtypes")
		Single dumbSingle();

		String string();
	}
}
