package com.restify.http.client.request;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.response.HttpResponseMessage;

public interface HttpClientRequest extends HttpRequestMessage {

	HttpResponseMessage execute() throws RestifyHttpException;

}
