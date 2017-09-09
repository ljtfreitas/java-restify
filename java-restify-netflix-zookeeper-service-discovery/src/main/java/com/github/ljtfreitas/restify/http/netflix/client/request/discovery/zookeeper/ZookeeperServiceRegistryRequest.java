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
package com.github.ljtfreitas.restify.http.netflix.client.request.discovery.zookeeper;

import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.ServiceRegistryRequest;

public class ZookeeperServiceRegistryRequest<T> implements ServiceRegistryRequest<ZookeeperServiceInstance> {

	private final ZookeeperServiceInstance instance;
	private final Payload<T> payload;

	public ZookeeperServiceRegistryRequest(ZookeeperServiceInstance instance, Payload<T> payload) {
		this.instance = instance;
		this.payload = payload;
	}

	@Override
	public ZookeeperServiceInstance instance() {
		return instance;
	}

	public Payload<T> payload() {
		return payload;
	}

	public static ZookeeperServiceRegistryRequestBuilder registry(ZookeeperServiceInstance instance) {
		return new ZookeeperServiceRegistryRequestBuilder(instance);
	}

	public static class ZookeeperServiceRegistryRequestBuilder {

		private final ZookeeperServiceInstance instance;

		private ZookeeperServiceRegistryRequestBuilder(ZookeeperServiceInstance instance) {
			this.instance = instance;
		}

		public <T> ZookeeperServiceRegistryRequest<T> with(Payload<T> payload) {
			return new ZookeeperServiceRegistryRequest<>(instance, payload);
		}

	}

	public static class Payload<T> {

		private final T payload;

		public Payload(T payload) {
			this.payload = payload;
		}

		public T get() {
			return payload;
		}

		public static <T> Payload<T> of(T payload) {
			return new Payload<>(payload);
		}
	}
}
