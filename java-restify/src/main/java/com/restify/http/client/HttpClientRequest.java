package com.restify.http.client;

import com.restify.http.RestifyHttpException;

public interface HttpClientRequest extends HttpRequestMessage {

	EndpointResponse execute() throws RestifyHttpException;

}
