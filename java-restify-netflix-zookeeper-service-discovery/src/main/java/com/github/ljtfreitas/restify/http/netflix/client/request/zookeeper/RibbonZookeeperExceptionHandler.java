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

import java.net.SocketException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonExceptionHandler;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonRequest;

public abstract class RibbonZookeeperExceptionHandler implements RibbonExceptionHandler {

	private final Collection<Class<? extends Throwable>> causes;

	public RibbonZookeeperExceptionHandler() {
		this(Arrays.asList(SocketException.class));
	}

	public RibbonZookeeperExceptionHandler(Collection<Class<? extends Throwable>> causes) {
		this.causes = new HashSet<>(causes);
	}

	@Override
	public final void onException(RibbonRequest request, Throwable cause) {
		if (found(cause)) {
			onConnectionFailure(request, cause);
		}
	}

	protected abstract void onConnectionFailure(RibbonRequest request, Throwable cause);

	private boolean found(Throwable cause) {
		return cause == null ? false : find(cause) ? true : found(cause.getCause());
	}

	private boolean find(Throwable cause) {
		return causes.stream().anyMatch(type -> type.isInstance(cause));
	}
}
