package com.restify.http.client.request;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.restify.http.client.response.EndpointResponseReader;
import com.restify.http.client.response.HttpResponseMessage;

public class RestifyEndpointRequestExecutor implements EndpointRequestExecutor {

	private final HttpClientRequestFactory httpClientRequestFactory;
	private final EndpointRequestWriter endpointRequestWriter;
	private final EndpointResponseReader endpointResponseReader;
	private final EndpointRequestInterceptorStack endpointRequestInterceptorStack;

	public RestifyEndpointRequestExecutor(HttpClientRequestFactory httpClientRequestFactory, EndpointRequestInterceptorStack endpointRequestInterceptorStack,
			EndpointRequestWriter endpointRequestWriter, EndpointResponseReader endpointResponseReader) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		this.endpointRequestInterceptorStack = endpointRequestInterceptorStack;
		this.endpointRequestWriter = endpointRequestWriter;
		this.endpointResponseReader = endpointResponseReader;
	}

	@Override
	public Object execute(EndpointRequest endpointRequest) {
		try (HttpResponseMessage response = doExecute(intercepts(endpointRequest))) {
			return responseOf(response, endpointRequest.expectedType());

		} catch (Exception e) {
			throw new RestifyHttpException(e);
		}
	}

	private HttpResponseMessage doExecute(EndpointRequest endpointRequest) {
		HttpClientRequest httpClientRequest = httpClientRequestFactory.createOf(endpointRequest);

		endpointRequest.body().ifPresent(b -> endpointRequestWriter.write(endpointRequest, httpClientRequest));

		return httpClientRequest.execute();
	}

	private EndpointRequest intercepts(EndpointRequest endpointRequest) {
		return endpointRequestInterceptorStack.apply(endpointRequest);
	}

	private Object responseOf(HttpResponseMessage response, ExpectedType expectedType) {
		return endpointResponseReader.read(response, expectedType);
	}
}
