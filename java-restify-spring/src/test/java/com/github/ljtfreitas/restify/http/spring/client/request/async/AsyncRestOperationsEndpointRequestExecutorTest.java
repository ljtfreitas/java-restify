package com.github.ljtfreitas.restify.http.spring.client.request.async;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.client.RestClientException;

import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.spring.client.request.EndpointResponseConverter;
import com.github.ljtfreitas.restify.http.spring.client.request.RequestEntityConverter;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class AsyncRestOperationsEndpointRequestExecutorTest {

	@Mock
	private AsyncRestOperations asyncRestOperationsMock;

	@Mock
	private RequestEntityConverter requestEntityConverterMock;

	@Mock
	private EndpointResponseConverter responseEntityConverterMock;

	@InjectMocks
	private AsyncRestOperationsEndpointRequestExecutor executor;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private URI endpointUri;

	private RequestEntity<Object> requestEntity;

	private ResponseEntity<Object> responseEntity;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		endpointUri = URI.create("http:/my.api.com/api");

		requestEntity = new RequestEntity<>(HttpMethod.GET, endpointUri);

		when(requestEntityConverterMock.convert(notNull(EndpointRequest.class)))
			.thenReturn(requestEntity);

		responseEntity = new ResponseEntity<>("expected result", HttpStatus.OK);

		when(asyncRestOperationsMock.exchange(eq(requestEntity.getUrl()), eq(requestEntity.getMethod()),
				same(requestEntity), notNull(ParameterizedTypeReference.class)))
			.thenReturn(new AsyncResult<>(responseEntity));

		when(responseEntityConverterMock.convert(notNull(ResponseEntity.class)))
			.thenReturn(new EndpointResponse<>(StatusCode.ok(), null, responseEntity.getBody()));
	}

	@Test
	public void shouldExecuteEndpointRequestWithSpringRestOperationsObject() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(endpointUri, "GET", String.class);

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(endpointRequest);

		EndpointResponse<Object> result = future.get();

		assertEquals(responseEntity.getBody(), result.body());

		verify(requestEntityConverterMock).convert(endpointRequest);

		verify(asyncRestOperationsMock).exchange(eq(requestEntity.getUrl()), eq(requestEntity.getMethod()),
				same(requestEntity), argThat(typeOf(String.class)));

		verify(responseEntityConverterMock)
			.convert(argThat(responseOf(responseEntity, JavaType.of(String.class))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldThrowRestifyHttpExceptionWhenRestTemplateThrowRestClientException() throws Exception {
		when(asyncRestOperationsMock.exchange(eq(requestEntity.getUrl()), eq(requestEntity.getMethod()),
				any(RequestEntity.class), any(ParameterizedTypeReference.class)))
			.thenReturn(AsyncResult.forExecutionException(new RestClientException("Spring RestClientException")));

		expectedException.expectCause(new BaseMatcher<Throwable>() {

			@Override
			public boolean matches(Object item) {
				Throwable cause = (Throwable) item;

				return cause instanceof HttpException
						&& cause.getCause() instanceof RestClientException;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(RestClientException.class.getName());
			}
		});

		CompletableFuture<EndpointResponse<Object>> future = executor.executeAsync(new EndpointRequest(endpointUri, "GET", String.class));

		future.get();
	}

	private ArgumentMatcher<ParameterizedTypeReference<Object>> typeOf(Type expectedType) {
		return new ArgumentMatcher<ParameterizedTypeReference<Object>>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object argument) {
				return ((ParameterizedTypeReference<Object>) argument).getType().equals(expectedType);
			}
		};
	}

	private ArgumentMatcher<ResponseEntity<Object>> responseOf(ResponseEntity<Object> responseEntity, JavaType expectedType) {
		return new ArgumentMatcher<ResponseEntity<Object>>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object argument) {
				ResponseEntity<Object> arg = (ResponseEntity<Object>) argument;
				return arg.getBody() == responseEntity.getBody();
			}
		};
	}
}
