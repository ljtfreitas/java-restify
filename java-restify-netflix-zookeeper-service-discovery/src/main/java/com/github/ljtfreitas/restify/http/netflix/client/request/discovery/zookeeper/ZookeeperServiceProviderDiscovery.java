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

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.DownInstancePolicy;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.ServiceProviderBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.ServiceInstance;
import com.github.ljtfreitas.restify.util.Tryable;

public class ZookeeperServiceProviderDiscovery<T> implements ZookeeperServiceDiscovery<T> {

	private final String serviceName;
	private final ZookeeperCuratorServiceDiscovery<T> serviceDiscovery;
	private final ServiceProvider<T> serviceProvider;

	public ZookeeperServiceProviderDiscovery(String serviceName, Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer) {
		this(serviceName, new ZookeeperCuratorServiceDiscovery<>(instanceType, configuration, serializer));
	}

	public ZookeeperServiceProviderDiscovery(String serviceName, Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer, CuratorFramework curator) {
		this(serviceName, new ZookeeperCuratorServiceDiscovery<>(instanceType, configuration, serializer, curator));
	}

	public ZookeeperServiceProviderDiscovery(String serviceName, ServiceProvider<T> serviceProvider) {
		this.serviceName = serviceName;
		this.serviceDiscovery = null;
		this.serviceProvider = serviceProvider;
	}

	public ZookeeperServiceProviderDiscovery(String serviceName, ZookeeperCuratorServiceDiscovery<T> serviceDiscovery) {
		this.serviceName = serviceName;
		this.serviceDiscovery = serviceDiscovery;
		this.serviceProvider = buildServiceProviderWith(serviceName, serviceDiscovery);
	}

	private ServiceProvider<T> buildServiceProviderWith(String serviceName, ZookeeperCuratorServiceDiscovery<T> serviceDiscovery) {
		try {
			ServiceProviderBuilder<T> builder = serviceDiscovery.serviceProviderBuilder();

			ServiceProvider<T> serviceProvider = builder.serviceName(serviceName)
				   .providerStrategy(new RoundRobinStrategy<>())
				   .downInstancePolicy(new DownInstancePolicy())
				   .build();

			serviceProvider.start();

			return serviceProvider;

		} catch (Exception e) {
			throw new ZookeeperServiceDiscoveryException("Error on create Zookeeper ServiceProvider for service [" + this.serviceName + "]", e);
		}
	}

	@Override
	public Collection<ZookeeperServiceInstance> queryForInstances(String serviceName) {
		isTrue(serviceName == null || serviceName.equalsIgnoreCase(this.serviceName),
				"ZookeeperServiceProviderDiscovery it's configured for services named [" + this.serviceName + "], "
						+ "not [" + serviceName + "]");

		return Tryable.of(() -> serviceProvider.getAllInstances()
				.stream().map(ZookeeperServiceInstance::of)
					.collect(Collectors.toList()));
	}

	@Override
	public void onFailure(ServiceInstance instance, Throwable cause) {
		serviceDiscovery.queryForInstance(new ZookeeperServiceInstance(instance))
			.ifPresent(server -> serviceProvider.noteError(server));
	}

	@Override
	public void close() throws IOException {
		Tryable.silently(serviceProvider::close);
		if (serviceDiscovery != null) {
			Tryable.silently(serviceDiscovery::close);
		}
	}
}
