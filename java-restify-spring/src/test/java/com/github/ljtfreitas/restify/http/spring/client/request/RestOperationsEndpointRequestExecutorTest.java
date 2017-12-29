package com.github.ljtfreitas.restify.http.spring.client.request;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.URI;

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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class RestOperationsEndpointRequestExecutorTest {

	@Mock
	private RestOperations restOperationsMock;

	@Mock
	private RequestEntityConverter requestEntityConverterMock;

	@Mock
	private EndpointResponseConverter responseEntityConverterMock;

	@InjectMocks
	private RestOperationsEndpointRequestExecutor executor;

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

		when(restOperationsMock.exchange(same(requestEntity), notNull(ParameterizedTypeReference.class)))
			.thenReturn(responseEntity);

		when(responseEntityConverterMock.convert(notNull(ResponseEntity.class)))
			.thenReturn(new EndpointResponse<>(StatusCode.ok(), null, responseEntity.getBody()));
	}

	@Test
	public void shouldExecuteEndpointRequestWithSpringRestOperationsObject() {
		EndpointRequest endpointRequest = new EndpointRequest(endpointUri, "GET", String.class);

		EndpointResponse<Object> result = executor.execute(endpointRequest);

		assertEquals(responseEntity.getBody(), result.body());

		verify(requestEntityConverterMock).convert(endpointRequest);

		verify(restOperationsMock).exchange(same(requestEntity), argThat(typeOf(String.class)));

		verify(responseEntityConverterMock)
			.convert(argThat(responseOf(responseEntity, JavaType.of(String.class))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldThrowRestifyHttpExceptionWhenRestTemplateThrowRestClientException() {
		when(restOperationsMock.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
			.thenThrow(new RestClientException("Spring RestClientException"));

		expectedException.expect(HttpException.class);
		expectedException.expectCause(isA(RestClientException.class));

		executor.execute(new EndpointRequest(endpointUri, "GET", String.class));
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
