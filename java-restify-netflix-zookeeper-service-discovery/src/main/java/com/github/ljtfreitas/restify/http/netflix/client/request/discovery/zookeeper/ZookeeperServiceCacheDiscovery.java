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
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceCacheBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;

import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.ServiceInstance;
import com.github.ljtfreitas.restify.util.Tryable;

public class ZookeeperServiceCacheDiscovery<T> implements ZookeeperServiceDiscovery<T> {

	private final String serviceName;
	private final ZookeeperCuratorServiceDiscovery<T> serviceDiscovery;
	private final ServiceCache<T> serviceCache;

	public ZookeeperServiceCacheDiscovery(String serviceName, Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer) {
		this(serviceName, new ZookeeperCuratorServiceDiscovery<>(instanceType, configuration, serializer));
	}

	public ZookeeperServiceCacheDiscovery(String serviceName, Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer, CuratorFramework curator) {
		this(serviceName, new ZookeeperCuratorServiceDiscovery<>(instanceType, configuration, serializer, curator));
	}

	public ZookeeperServiceCacheDiscovery(String serviceName, ServiceCache<T> serviceCache) {
		this.serviceName = serviceName;
		this.serviceDiscovery = null;
		this.serviceCache = serviceCache;
	}

	public ZookeeperServiceCacheDiscovery(String serviceName, ZookeeperCuratorServiceDiscovery<T> serviceDiscovery) {
		this.serviceName = serviceName;
		this.serviceDiscovery = serviceDiscovery;
		this.serviceCache = buildServiceCacheWith(serviceName, serviceDiscovery);
	}

	private ServiceCache<T> buildServiceCacheWith(String serviceName, ZookeeperCuratorServiceDiscovery<T> serviceDiscovery) {
		try {
			ServiceCacheBuilder<T> builder = serviceDiscovery.serviceCacheBuilder();

			ServiceCache<T> serviceCache = builder
					.name(serviceName)
					.build();

			serviceCache.start();

			return serviceCache;

		} catch (Exception e) {
			throw new ZookeeperServiceDiscoveryException("Error on create Zookeeper ServiceProvider for service [" + this.serviceName + "]", e);
		}
	}

	@Override
	public Collection<ZookeeperServiceInstance> queryForInstances(String serviceName) {
		isTrue(serviceName == null || serviceName.equalsIgnoreCase(this.serviceName),
				"ZookeeperServiceCacheDiscovery it's configured for services named [" + this.serviceName + "], "
						+ "not [" + serviceName + "]");

		return Tryable.of(() -> serviceCache.getInstances()
				.stream().map(ZookeeperServiceInstance::of)
					.collect(Collectors.toList()));
	}

	@Override
	public void onFailure(ServiceInstance instance, Throwable cause) {
		Tryable.run(() -> serviceDiscovery.unregisterService(new ZookeeperServiceInstance(instance)));
	}

	@Override
	public void close() throws IOException {
		Tryable.silently(serviceCache::close);
		if (serviceDiscovery != null) {
			serviceDiscovery.close();
		}
	}
}
