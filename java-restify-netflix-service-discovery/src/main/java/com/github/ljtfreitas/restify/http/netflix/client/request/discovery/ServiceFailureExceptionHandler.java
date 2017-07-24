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
package com.github.ljtfreitas.restify.http.netflix.client.request.discovery;

import java.net.SocketException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonExceptionHandler;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonRequest;

public class ServiceFailureExceptionHandler implements RibbonExceptionHandler {

	private final ServiceInstanceFailureHandler handler;
	private final Collection<Class<? extends Throwable>> causes;

	public ServiceFailureExceptionHandler(ServiceInstanceFailureHandler handler) {
		this(handler, Arrays.asList(SocketException.class));
	}

	public ServiceFailureExceptionHandler(ServiceInstanceFailureHandler handler, Collection<Class<? extends Throwable>> causes) {
		this.handler = handler;
		this.causes = new HashSet<>(causes);
	}

	@Override
	public final void onException(RibbonRequest request, Throwable cause) {
		if (connectionException(cause)) {
			onConnectionFailure(request, cause);
		}
	}

	private void onConnectionFailure(RibbonRequest request, Throwable cause) {
		ServiceInstance instance = new ServiceInstance() {

			@Override
			public int port() {
				return request.getUri().getPort();
			}

			@Override
			public String name() {
				return request.serviceName();
			}

			@Override
			public String host() {
				return request.getUri().getHost();
			}
		};

		handler.onFailure(instance, cause);
	}

	private boolean connectionException(Throwable cause) {
		return cause == null ? false : find(cause) ? true : connectionException(cause.getCause());
	}

	private boolean find(Throwable cause) {
		return causes.stream().anyMatch(type -> type.isInstance(cause));
	}
}
