package com.restify.http.client.spring;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
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
import org.springframework.web.client.RestOperations;

import com.restify.http.client.request.ExpectedType;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class RestOperationsEndpointRequestExecutorTest {

	@Mock
	private RestOperations restOperationsMock;

	@Mock
	private RequestEntityConverter requestEntityConverterMock;

	@Mock
	private ResponseEntityConverter responseEntityConverterMock;

	@InjectMocks
	private RestOperationsEndpointRequestExecutor executor;

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

		when(responseEntityConverterMock.convert(notNull(EndpointResponseEntity.class)))
			.thenReturn(responseEntity.getBody());
	}

	@Test
	public void shouldExecuteEndpointRequestWithSpringRestOperationsObject() {
		EndpointRequest endpointRequest = new EndpointRequest(endpointUri, "GET", String.class);

		Object result = executor.execute(endpointRequest);

		assertEquals(responseEntity.getBody(), result);

		verify(requestEntityConverterMock).convert(endpointRequest);

		verify(restOperationsMock).exchange(same(requestEntity), argThat(typeOf(String.class)));

		verify(responseEntityConverterMock)
			.convert(argThat(responseOf(responseEntity, ExpectedType.of(String.class))));
	}

	@Test
	public void shouldExecuteEndpointRequestWithCorrectExpectedBodyTypeWhenExpectedMethodReturnIsResponseEntity() {
		SimpleParameterizedType expectedType = new SimpleParameterizedType(ResponseEntity.class, null, String.class);

		EndpointRequest endpointRequest = new EndpointRequest(endpointUri, "GET",
				expectedType);

		Object result = executor.execute(endpointRequest);

		assertEquals(responseEntity.getBody(), result);

		verify(requestEntityConverterMock).convert(endpointRequest);

		verify(restOperationsMock).exchange(same(requestEntity), argThat(typeOf(String.class)));

		verify(responseEntityConverterMock)
			.convert(argThat(responseOf(responseEntity, ExpectedType.of(expectedType, ResponseEntity.class))));
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

	private ArgumentMatcher<EndpointResponseEntity> responseOf(ResponseEntity<Object> responseEntity, ExpectedType expectedType) {
		return new ArgumentMatcher<EndpointResponseEntity>() {
			@Override
			public boolean matches(Object argument) {
				EndpointResponseEntity arg = (EndpointResponseEntity) argument;
				return arg.entity() == responseEntity && arg.expectedType().equals(expectedType);
			}
		};
	}
}
