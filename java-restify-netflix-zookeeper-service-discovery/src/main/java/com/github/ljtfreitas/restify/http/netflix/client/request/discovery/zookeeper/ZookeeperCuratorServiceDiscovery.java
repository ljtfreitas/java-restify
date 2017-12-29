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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.x.discovery.ServiceCacheBuilder;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProviderBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;

import com.github.ljtfreitas.restify.util.Tryable;

public class ZookeeperCuratorServiceDiscovery<T> implements Closeable {

	private final ServiceDiscovery<T> serviceDiscovery;
	private final CuratorFramework curator;

	public ZookeeperCuratorServiceDiscovery(Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer) {
		CuratorFramework curator = buildCuratorWith(configuration);
		this.serviceDiscovery = buildServiceDiscoveryWith(instanceType, configuration, serializer, curator);
		this.curator = curator;
	}

	public ZookeeperCuratorServiceDiscovery(Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer, CuratorFramework curator) {
		this.serviceDiscovery = buildServiceDiscoveryWith(instanceType, configuration, serializer, curator);
		this.curator = curator;
	}

	public CuratorFramework buildCuratorWith(ZookeeperConfiguration configuration) {
		return CuratorFrameworkFactory.builder()
				.connectString(configuration.connectionString())
				.retryPolicy(new RetryNever())
					.build();
	}

	private ServiceDiscovery<T> buildServiceDiscoveryWith(Class<T> instanceType, ZookeeperConfiguration configuration,
			InstanceSerializer<T> serializer, CuratorFramework curator) {
		try {
			if (!CuratorFrameworkState.STARTED.equals(curator.getState())) {
				curator.start();
			}

			ServiceDiscovery<T> serviceDiscovery = ServiceDiscoveryBuilder.builder(instanceType)
					.client(curator)
						.basePath(configuration.chroot())
						.serializer(serializer)
							.build();

			serviceDiscovery.start();

			return serviceDiscovery;
		} catch (Exception e) {
			throw new ZookeeperServiceDiscoveryException("Error on create Zookeeper ServiceDiscovery", e);
		}
	}

	public Collection<ServiceInstance<T>> queryForInstances(String serviceName) throws Exception {
		return serviceDiscovery.queryForInstances(serviceName);
	}

	public Optional<ServiceInstance<T>> queryForInstance(String name, String id) throws Exception {
		return Optional.ofNullable(serviceDiscovery.queryForInstance(name, id));
	}

	public Optional<ServiceInstance<T>> queryForInstance(ZookeeperServiceInstance instance) {
		return doQueryForInstance(instance);
	}

	private Optional<ServiceInstance<T>> doQueryForInstance(ZookeeperServiceInstance instance) {
		return Tryable.of(() -> serviceDiscovery.queryForInstances(instance.name()))
				.stream()
					.filter(i -> i.getAddress().equals(instance.host()) && i.getPort().equals(instance.port()))
						.findFirst();
	}

	public Collection<String> queryForNames() throws Exception {
		return serviceDiscovery.queryForNames();
	}

	public void registerService(ServiceInstance<T> serviceInstance) throws Exception {
		serviceDiscovery.registerService(serviceInstance);
	}

	public void unregisterService(ServiceInstance<T> serviceInstance) throws Exception {
		serviceDiscovery.unregisterService(serviceInstance);
	}

	public void unregisterService(ZookeeperServiceInstance serviceInstance) throws Exception {
		doQueryForInstance(serviceInstance)
			.ifPresent(server -> Tryable.run(() -> serviceDiscovery.unregisterService(server)));
	}

	public void updateService(ServiceInstance<T> serviceInstance) throws Exception {
		serviceDiscovery.updateService(serviceInstance);
	}

	public ServiceProviderBuilder<T> serviceProviderBuilder() {
		return serviceDiscovery.serviceProviderBuilder();
	}

	public ServiceCacheBuilder<T> serviceCacheBuilder() {
		return serviceDiscovery.serviceCacheBuilder();
	}

	@Override
	public final void close() throws IOException {
		if (!CuratorFrameworkState.STOPPED.equals(curator.getState())) {
			Tryable.silently(serviceDiscovery::close);
			Tryable.silently(curator::close);
		}
	}

	private class RetryNever implements RetryPolicy {

		@Override
		public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
			return false;
		}

	}
}
