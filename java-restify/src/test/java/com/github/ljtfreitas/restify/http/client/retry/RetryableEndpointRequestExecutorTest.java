package com.github.ljtfreitas.restify.http.client.retry;

import static org.hamcrest.Matchers.sameInstance;
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestMetadata;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseConflictException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseInternalServerErrorException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseUnauthorizedException;

@RunWith(MockitoJUnitRunner.class)
public class RetryableEndpointRequestExecutorTest {

	@Mock
	private EndpointRequestExecutor delegate;

	@InjectMocks
	private RetryableEndpointRequestExecutor executor;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoop() {
		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));

		RetryableEndpointRequest request = new RetryableEndpointRequest();

		EndpointResponse<String> output = executor.execute(request);

		verify(delegate, times(1)).execute(request);

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithStatusCodeCollection() {
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

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."))
			.thenThrow(new EndpointResponseUnauthorizedException("2st attempt...", Headers.empty(), "2st error..."))
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		EndpointResponse<String> output = executor.execute(request);

		verify(delegate, times(3)).execute(request);

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithStatusCodeCollection() {
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

		EndpointResponseUnauthorizedException unretryableException = new EndpointResponseUnauthorizedException("2st attempt...",
				Headers.empty(), "2st error...");

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."))
			.thenThrow(unretryableException)
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		expectedException.expect(sameInstance(unretryableException));

		executor.execute(request);

		verify(delegate, times(2)).execute(request);
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithOn5xxParameter() {
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

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."))
			.thenThrow(new EndpointResponseInternalServerErrorException("2st attempt...", Headers.empty(), "2st error..."))
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		EndpointResponse<String> output = executor.execute(request);

		verify(delegate, times(3)).execute(request);

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithOn5xxParameter() {
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

		EndpointResponseUnauthorizedException unretryableException = new EndpointResponseUnauthorizedException("2st attempt...",
				Headers.empty(), "2st error...");

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."))
			.thenThrow(unretryableException)
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		expectedException.expect(sameInstance(unretryableException));

		executor.execute(request);

		verify(delegate, times(2)).execute(request);
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithOn4xxParameter() {
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

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new EndpointResponseConflictException("1st attempt...", Headers.empty(), "1st error..."))
			.thenThrow(new EndpointResponseConflictException("2st attempt...", Headers.empty(), "2st error..."))
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		EndpointResponse<String> output = executor.execute(request);

		verify(delegate, times(3)).execute(request);

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithOn4xxParameter() {
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

		EndpointResponseInternalServerErrorException unretryableException = new EndpointResponseInternalServerErrorException("2st attempt...",
				Headers.empty(), "2st error...");

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new EndpointResponseConflictException("1st attempt...", Headers.empty(), "1st error..."))
			.thenThrow(unretryableException)
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		expectedException.expect(sameInstance(unretryableException));

		executor.execute(request);

		verify(delegate, times(2)).execute(request);
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithOnIOFailureParameter() {
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

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new RuntimeException(new IOException("1st attempt...")))
			.thenThrow(new RuntimeException(new UnknownHostException("2st attempt...")))
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		EndpointResponse<String> output = executor.execute(request);

		verify(delegate, times(3)).execute(request);

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithOnIOFailureParameter() {
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

		IllegalStateException unretryableException = new IllegalStateException("2st attempt...");

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new RuntimeException(new InterruptedIOException("1st attempt...")))
			.thenThrow(unretryableException)
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		expectedException.expect(sameInstance(unretryableException));

		executor.execute(request);

		verify(delegate, times(2)).execute(request);
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnLoopUsingRetryMetadataWithExceptionCollection() {
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

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new RuntimeException(new SocketException("1st attempt...")))
			.thenThrow(new RuntimeException(new SocketException("2st attempt...")))
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		EndpointResponse<String> output = executor.execute(request);

		verify(delegate, times(3)).execute(request);

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());
	}

	@Test
	public void shouldThrowUnretryableExceptionUsingRetryMetadataWithExceptionCollection() {
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

		IllegalStateException unretryableException = new IllegalStateException("2st attempt...");

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new RuntimeException(new SocketException("1st attempt...")))
			.thenThrow(unretryableException)
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest(retry);

		expectedException.expect(sameInstance(unretryableException));

		executor.execute(request);

		verify(delegate, times(2)).execute(request);
	}

	@Test
	public void shouldWrapEndpointRequestExecutorOnRetryLoopUsingConfiguration() {
		RetryConfiguration configuration = new RetryConfiguration.Builder()
				.when(HttpStatusCode.INTERNAL_SERVER_ERROR, HttpStatusCode.UNAUTHORIZED)
				.attempts(3)
				.build();

		when(delegate.execute(notNull(EndpointRequest.class)))
			.thenThrow(new EndpointResponseInternalServerErrorException("1st attempt...", Headers.empty(), "1st error..."))
			.thenThrow(new EndpointResponseUnauthorizedException("2st attempt...", Headers.empty(), "2st error..."))
			.thenReturn(new EndpointResponse<>(StatusCode.of(HttpStatusCode.OK), "success"));;

		RetryableEndpointRequest request = new RetryableEndpointRequest();

		executor = new RetryableEndpointRequestExecutor(delegate, configuration);

		EndpointResponse<String> output = executor.execute(request);

		verify(delegate, times(3)).execute(request);

		assertTrue(output.status().isOk());
		assertEquals("success", output.body());
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
