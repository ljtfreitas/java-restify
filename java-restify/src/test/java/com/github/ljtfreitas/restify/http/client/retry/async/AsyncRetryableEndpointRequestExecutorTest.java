package com.github.ljtfreitas.restify.http.client.retry.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.annotation.Annotation;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestMetadata;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseConflictException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseInternalServerErrorException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseUnauthorizedException;
import com.github.ljtfreitas.restify.http.client.retry.BackOff;
import com.github.ljtfreitas.restify.http.client.retry.Retry;
import com.github.ljtfreitas.restify.http.client.retry.RetryConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class AsyncRetryableEndpointRequestExecutorTest {

	@Mock
	private AsyncEndpointRequestExecutor asyncEndpointRequestExecutor;

	private ScheduledExecutorService scheduler;

	private AsyncRetryableEndpointRequestExecutor executor;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		scheduler = Executors.newSingleThreadScheduledExecutor();

		executor = new AsyncRetryableEndpointRequestExecutor(asyncEndpointRequestExecutor, scheduler);
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoop() throws Exception {
		when(asyncEndpointRequestExecutor.executeAsync(notNull(EndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success")));

		RetryableEndpointRequest request = new RetryableEndpointRequest();

		CompletableFuture<EndpointResponse<String>> future = executor.executeAsync(request);

		EndpointResponse<String> output = future.get();

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());

		verify(asyncEndpointRequestExecutor, times(1)).executeAsync(request);
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithStatusCodeCollection() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[] { HttpStatusCode.INTERNAL_SERVER_ERROR, HttpStatusCode.UNAUTHORIZED };
			}

			@Override
			public boolean on5xxStatus() {
				return false;
			}

			@Override
			public boolean on4xxStatus() {
				return false;
			}

			@Override
			public boolean onIOFailure() {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[0];
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."));

		CompletableFuture<EndpointResponse<Object>> second = new CompletableFuture<>();
		second.completeExceptionally(new EndpointResponseUnauthorizedException("2st attempt...", Headers.empty(), "2st error..."));

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		when(asyncEndpointRequestExecutor.executeAsync(notNull(EndpointRequest.class)))
			.thenReturn(first)
			.thenReturn(second)
			.thenReturn(third);

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		EndpointResponse<Object> output = future.get();

		verify(asyncEndpointRequestExecutor, times(3)).executeAsync(request);

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithStatusCodeCollection() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[] { HttpStatusCode.INTERNAL_SERVER_ERROR };
			}

			@Override
			public boolean on5xxStatus() {
				return false;
			}

			@Override
			public boolean on4xxStatus() {
				return false;
			}

			@Override
			public boolean onIOFailure() {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[0];
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."));

		CompletableFuture<EndpointResponse<Object>> unretryableFuture = new CompletableFuture<>();
		unretryableFuture.completeExceptionally(new EndpointResponseUnauthorizedException("2st attempt...", Headers.empty(), "2st error..."));

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(unretryableFuture)
			.thenReturn(third);

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		Thread.sleep(1000);

		assertTrue(future.isCompletedExceptionally());

		verify(asyncEndpointRequestExecutor, times(2)).executeAsync(request);
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithOn5xxParameter() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[0];
			}

			@Override
			public boolean on5xxStatus() {
				return true;
			}

			@Override
			public boolean on4xxStatus() {
				return false;
			}

			@Override
			public boolean onIOFailure() {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[0];
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."));

		CompletableFuture<EndpointResponse<Object>> second = new CompletableFuture<>();
		second.completeExceptionally(new EndpointResponseInternalServerErrorException("2st attempt...", Headers.empty(), "2st error..."));

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(second)
			.thenReturn(third);

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		EndpointResponse<Object> output = future.get();

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());

		verify(asyncEndpointRequestExecutor, times(3)).executeAsync(request);
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithOn5xxParameter() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[0];
			}

			@Override
			public boolean on5xxStatus() {
				return true;
			}

			@Override
			public boolean on4xxStatus() {
				return false;
			}

			@Override
			public boolean onIOFailure() {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[0];
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."));

		CompletableFuture<EndpointResponse<Object>> unretryableFuture = new CompletableFuture<>();
		EndpointResponseUnauthorizedException unretryableException = new EndpointResponseUnauthorizedException("2st attempt...",
				Headers.empty(), "2st error...");
		unretryableFuture.completeExceptionally(unretryableException);

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(unretryableFuture)
			.thenReturn(third);

		expectedException.expectCause(deepCause(unretryableException));

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		Thread.sleep(2000);

		verify(asyncEndpointRequestExecutor, times(2)).executeAsync(request);

		future.get();
	}


	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithOn4xxParameter() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[0];
			}

			@Override
			public boolean on5xxStatus() {
				return false;
			}

			@Override
			public boolean on4xxStatus() {
				return true;
			}

			@Override
			public boolean onIOFailure() {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[0];
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new EndpointResponseConflictException("1st attempt...", Headers.empty(), "1st error..."));

		CompletableFuture<EndpointResponse<Object>> second = new CompletableFuture<>();
		second.completeExceptionally(new EndpointResponseConflictException("2st attempt...", Headers.empty(), "2st error..."));

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(second)
			.thenReturn(third);

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		EndpointResponse<Object> output = future.get();

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());

		verify(asyncEndpointRequestExecutor, times(3)).executeAsync(request);
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithOn4xxParameter() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[0];
			}

			@Override
			public boolean on5xxStatus() {
				return false;
			}

			@Override
			public boolean on4xxStatus() {
				return true;
			}

			@Override
			public boolean onIOFailure() {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[0];
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};


		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new EndpointResponseConflictException("1st attempt...", Headers.empty(), "1st error..."));

		CompletableFuture<EndpointResponse<Object>> unretryableFuture = new CompletableFuture<>();
		EndpointResponseInternalServerErrorException unretryableException = new EndpointResponseInternalServerErrorException("2st attempt...",
				Headers.empty(), "2st error...");
		unretryableFuture.completeExceptionally(unretryableException);

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(unretryableFuture)
			.thenReturn(third);

		expectedException.expect(deepCause(unretryableException));

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		Thread.sleep(2000);

		verify(asyncEndpointRequestExecutor, times(2)).executeAsync(request);

		future.get();
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithOnIOFailureParameter() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[0];
			}

			@Override
			public boolean on5xxStatus() {
				return false;
			}

			@Override
			public boolean on4xxStatus() {
				return false;
			}

			@Override
			public boolean onIOFailure() {
				return true;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[0];
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new RuntimeException(new IOException("1st attempt...")));

		CompletableFuture<EndpointResponse<Object>> second = new CompletableFuture<>();
		second.completeExceptionally(new RuntimeException(new UnknownHostException("2st attempt...")));

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(second)
			.thenReturn(third);

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		EndpointResponse<Object> output = future.get();

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());

		verify(asyncEndpointRequestExecutor, times(3)).executeAsync(request);
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithOnIOFailureParameter() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[0];
			}

			@Override
			public boolean on5xxStatus() {
				return false;
			}

			@Override
			public boolean on4xxStatus() {
				return false;
			}

			@Override
			public boolean onIOFailure() {
				return true;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[0];
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new RuntimeException(new InterruptedIOException("1st attempt...")));

		CompletableFuture<EndpointResponse<Object>> unretryableFuture = new CompletableFuture<>();
		IllegalStateException unretryableException = new IllegalStateException("2st attempt...");
		unretryableFuture.completeExceptionally(unretryableException);

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(unretryableFuture)
			.thenReturn(third);

		expectedException.expectCause(deepCause(unretryableException));

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		Thread.sleep(2000);

		verify(asyncEndpointRequestExecutor, times(2)).executeAsync(request);

		future.get();
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithExceptionCollection() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[0];
			}

			@Override
			public boolean on5xxStatus() {
				return false;
			}

			@Override
			public boolean on4xxStatus() {
				return false;
			}

			@Override
			public boolean onIOFailure() {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[] { SocketException.class };
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new RuntimeException(new SocketException("1st attempt...")));

		CompletableFuture<EndpointResponse<Object>> second = new CompletableFuture<>();
		second.completeExceptionally(new RuntimeException(new SocketException("2st attempt...")));

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(second)
			.thenReturn(third);

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		EndpointResponse<Object> output = future.get();

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());

		verify(asyncEndpointRequestExecutor, times(3)).executeAsync(request);
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithExceptionCollection() throws Exception {
		Retry retry = new Retry() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Retry.class;
			}

			@Override
			public int timeout() {
				return 0;
			}

			@Override
			public HttpStatusCode[] status() {
				return new HttpStatusCode[0];
			}

			@Override
			public boolean on5xxStatus() {
				return false;
			}

			@Override
			public boolean on4xxStatus() {
				return false;
			}

			@Override
			public boolean onIOFailure() {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends Throwable>[] exceptions() {
				return new Class[] { SocketException.class };
			}

			@Override
			public BackOff backoff() {
				return new BackOff() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return BackOff.class;
					}

					@Override
					public double multiplier() {
						return 0;
					}

					@Override
					public long delay() {
						return 0;
					}
				};
			}

			@Override
			public int attempts() {
				return 3;
			}
		};

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new RuntimeException(new SocketException("1st attempt...")));

		CompletableFuture<EndpointResponse<Object>> unretryableFuture = new CompletableFuture<>();
		IllegalStateException unretryableException = new IllegalStateException("2st attempt...");
		unretryableFuture.completeExceptionally(unretryableException);

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(unretryableFuture)
			.thenReturn(third);

		expectedException.expectCause(deepCause(unretryableException));

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		Thread.sleep(2000);

		verify(asyncEndpointRequestExecutor, times(2)).executeAsync(request);

		future.get();
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnRetryLoopUsingConfiguration() throws Exception {
		RetryConfiguration configuration = new RetryConfiguration.Builder()
				.when(HttpStatusCode.INTERNAL_SERVER_ERROR, HttpStatusCode.UNAUTHORIZED)
				.attempts(3)
				.build();

		CompletableFuture<EndpointResponse<Object>> first = new CompletableFuture<>();
		first.completeExceptionally(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."));

		CompletableFuture<EndpointResponse<Object>> second = new CompletableFuture<>();
		second.completeExceptionally(new EndpointResponseUnauthorizedException("2st attempt...", Headers.empty(), "2st error..."));

		CompletableFuture<EndpointResponse<Object>> third = CompletableFuture.completedFuture(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest();

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(first)
			.thenReturn(second)
			.thenReturn(third);

		executor = new AsyncRetryableEndpointRequestExecutor(asyncEndpointRequestExecutor, scheduler, configuration);

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(request);

		EndpointResponse<Object> output = future.get();

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());

		verify(asyncEndpointRequestExecutor, times(3)).executeAsync(request);
	}

	private Matcher<? extends Throwable> deepCause(Exception expectedCause) {
		return new BaseMatcher<Throwable>() {

			@Override
			public boolean matches(Object argument) {
				Throwable exception = (Throwable) argument;
				Throwable cause = exception.getCause();

				while (cause != null) {
					if (expectedCause.equals(cause)) return true;
					cause = cause.getCause();
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(expectedCause.getClass().getName());
			}

		};
	}

	private class RetryableEndpointRequest extends EndpointRequest {

		public RetryableEndpointRequest() {
			super(URI.create("http://my.api.com/retry"), "GET");
		}

		public RetryableEndpointRequest(Retry retry) {
			super(URI.create("http://my.api.com/retry"), "GET", null, null, Object.class, null,
					new EndpointRequestMetadata(Arrays.asList(retry)));
		}
	}
 }
