package com.restify.http.client.request;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.response.EndpointResponseReader;
import com.restify.http.client.response.HttpResponseMessage;

public class RestifyEndpointRequestExecutor implements EndpointRequestExecutor {

	private final HttpClientRequestFactory httpClientRequestFactory;
	private final EndpointRequestWriter endpointRequestWriter;
	private final EndpointResponseReader endpointResponseReader;

	public RestifyEndpointRequestExecutor(HttpClientRequestFactory httpClientRequestFactory,
			EndpointRequestWriter endpointRequestWriter, EndpointResponseReader endpointResponseReader) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		this.endpointRequestWriter = endpointRequestWriter;
		this.endpointResponseReader = endpointResponseReader;
	}

	@Override
	public Object execute(EndpointRequest endpointRequest) {
		try (HttpResponseMessage response = doExecute(endpointRequest)) {
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

	private Object responseOf(HttpResponseMessage response, ExpectedType expectedType) {
		return endpointResponseReader.read(response, expectedType);
	}
}
