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

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;

import com.github.ljtfreitas.restify.util.Tryable;

public class DefaultZookeeperServiceRegistry<T> implements ZookeeperServiceRegistry<T> {

	private final ZookeeperCuratorServiceDiscovery<T> serviceDiscovery;

	public DefaultZookeeperServiceRegistry(Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer) {
		this(new ZookeeperCuratorServiceDiscovery<>(instanceType, configuration, serializer));
	}

	public DefaultZookeeperServiceRegistry(Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer, CuratorFramework curator) {
		this(new ZookeeperCuratorServiceDiscovery<>(instanceType, configuration, serializer, curator));
	}

	public DefaultZookeeperServiceRegistry(ZookeeperCuratorServiceDiscovery<T> serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	@Override
	public void close() throws IOException {
		serviceDiscovery.close();
	}

	@Override
	public void register(ZookeeperServiceRegistryRequest<T> request) {
		ZookeeperServiceInstance instance = request.instance();

		try {
			ServiceInstanceBuilder<T> builder = ServiceInstance.builder();

			builder.name(instance.name())
				.id(instance.id())
				.payload(request.payload().get())
				.address(instance.host())
				.uriSpec(new UriSpec("{scheme}://{address}:{port}"));

			if (instance.sslPort().isPresent()) {
				builder.sslPort(instance.sslPort().get());
			} else {
				builder.port(instance.port());
			}

			serviceDiscovery.registerService(builder.build());

		} catch (Exception e) {
			throw new ZookeeperServiceDiscoveryException("Exception on register Zookeeper service instance [" + instance + "]" , e);
		}
	}

	@Override
	public void unregister(ZookeeperServiceInstance instance) {
		try {
			serviceDiscovery.queryForInstance(instance)
				.ifPresent(service -> Tryable.run(() -> serviceDiscovery.unregisterService(service)));

		} catch (Exception e) {
			throw new ZookeeperServiceDiscoveryException("Exception on unregister Zookeeper service instance [" + instance + "]" , e);
		}
	}
}
