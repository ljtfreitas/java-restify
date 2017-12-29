/*******************************************************************************
*
* MIT License
*
* Copyright (c) 2016 Tiago de Freitas Lima
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*
*******************************************************************************/
package com.github.ljtfreitas.restify.http.client.call.async;

import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseException;

public abstract class EndpointResponseFailureCallback implements EndpointCallFailureCallback {

	@Override
	public final void onFailure(Throwable throwable) {
		if (throwable instanceof EndpointResponseException) {
			EndpointResponseException exception = (EndpointResponseException) throwable;
			onResponseFailure(exception);

		} else {
			onException(throwable);
		}
	}

	protected void onException(Throwable throwable) {
		HttpException newException = (throwable instanceof HttpException) ?
				(HttpException) throwable : new HttpException(throwable);
		throw newException;
	}

	protected void onResponseFailure(EndpointResponseException exception) {
		StatusCode responseStatusCode = exception.status();

		EndpointResponse<String> response = exception.response();

		if (responseStatusCode.isBadRequest()) {
			onBadRequest(response);

		} else if (responseStatusCode.isUnauthorized()) {
			onUnauthorized(response);

		} else if (responseStatusCode.isForbidden()) {
			onForbidden(response);

		} else if (responseStatusCode.isNotFound()) {
			onNotFound(response);

		} else if (responseStatusCode.isMethodNotAllowed()) {
			onMethodNotAllowed(response);

		} else if (responseStatusCode.isNotAcceptable()) {
			onNotAcceptable(response);

		} else if (responseStatusCode.isProxyAuthenticationRequired()) {
			onProxyAuthenticationRequired(response);

		} else if (responseStatusCode.isRequestTimeout()) {
			onRequestTimeout(response);

		} else if (responseStatusCode.isConflict()) {
			onConflict(response);

		} else if (responseStatusCode.isGone()) {
			onGone(response);

		} else if (responseStatusCode.isLengthRequired()) {
			onLengthRequired(response);

		} else if (responseStatusCode.isPreconditionFailed()) {
			onPreconditionFailed(response);

		} else if (responseStatusCode.isRequestEntityTooLarge()) {
			onRequestEntityTooLarge(response);

		} else if (responseStatusCode.isRequestUriTooLong()) {
			onRequestUriTooLong(response);

		} else if (responseStatusCode.isUnsupportedMediaType()) {
			onUnsupportedMediaType(response);

		} else if (responseStatusCode.isRequestedRangeNotSatisfiable()) {
			onRequestedRangeNotSatisfiable(response);

		} else if (responseStatusCode.isExpectationFailed()) {
			onExpectationFailed(response);

		} else if (responseStatusCode.isInternalServerError()) {
			onInternalServerError(response);

		} else if (responseStatusCode.isNotImplemented()) {
			onNotImplemented(response);

		} else if (responseStatusCode.isBadGateway()) {
			onBadGateway(response);

		} else if (responseStatusCode.isServiceUnavailable()) {
			onServiceUnavailable(response);

		} else if (responseStatusCode.isGatewayTimeout()) {
			onGatewayTimeout(response);

		} else if (responseStatusCode.isHttpVersionNotSupported()) {
			onHttpVersionNotSupported(response);

		} else {
			unhandled(response);
		}
	}

	protected void onBadRequest(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onUnauthorized(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onForbidden(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onNotFound(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onMethodNotAllowed(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onNotAcceptable(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onProxyAuthenticationRequired(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onRequestTimeout(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onConflict(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onGone(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onLengthRequired(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onPreconditionFailed(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onRequestEntityTooLarge(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onRequestUriTooLong(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onUnsupportedMediaType(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onRequestedRangeNotSatisfiable(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onExpectationFailed(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onInternalServerError(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onNotImplemented(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onBadGateway(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onServiceUnavailable(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onGatewayTimeout(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void onHttpVersionNotSupported(EndpointResponse<String> response) {
		unhandled(response);
	}

	protected void unhandled(EndpointResponse<String> response) {
	}
}
