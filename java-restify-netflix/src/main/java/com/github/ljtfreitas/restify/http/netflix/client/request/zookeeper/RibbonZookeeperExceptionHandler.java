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
package com.github.ljtfreitas.restify.http.netflix.client.request.zookeeper;

import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonRequest;

import java.net.SocketException;

import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonExceptionHandler;

public class RibbonZookeeperExceptionHandler implements RibbonExceptionHandler {

	private final ZookeeperServiceDiscovery zookeeperServiceDiscovery;

	public RibbonZookeeperExceptionHandler(ZookeeperServiceDiscovery zookeeperServiceDiscovery) {
		this.zookeeperServiceDiscovery = zookeeperServiceDiscovery;
	}

	@Override
	public void onException(RibbonRequest request, Throwable cause) {
		if (isSocketException(cause)) {
			String name = request.serviceName();
			String host = request.getUri().getHost();
			int port = request.getUri().getPort();

			zookeeperServiceDiscovery.unregister(new ZookeeperInstance(name, host, port));
		}
	}

	private boolean isSocketException(Throwable cause) {
		Throwable throwable = cause;

		boolean isSocketException = false;

		while (throwable != null) {
			isSocketException = (throwable instanceof SocketException);

			if (isSocketException) {
				break;

			} else {
				throwable = throwable.getCause();
			}
		}

		return isSocketException;
	}
}
