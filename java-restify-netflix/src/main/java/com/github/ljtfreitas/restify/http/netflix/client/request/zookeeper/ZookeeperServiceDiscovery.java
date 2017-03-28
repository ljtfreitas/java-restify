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

import java.util.Collection;
import java.util.Optional;

import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;

import com.github.ljtfreitas.restify.http.util.Tryable;

public class ZookeeperServiceDiscovery {

	private final ServiceDiscovery<ZookeeperInstance> serviceDiscovery;
	private final CuratorFramework curator;

	public ZookeeperServiceDiscovery(ZookeeperConfiguration configuration) {
		CuratorFramework curator = buildCuratorWith(configuration);
		this.serviceDiscovery = buildServiceDiscoveryWith(configuration, curator, new ZookeeperInstanceSerializer());
		this.curator = curator;
	}

	public ZookeeperServiceDiscovery(ZookeeperConfiguration configuration, CuratorFramework curator) {
		this(configuration, curator, new ZookeeperInstanceSerializer());
	}

	public ZookeeperServiceDiscovery(ZookeeperConfiguration configuration, CuratorFramework curator, InstanceSerializer<ZookeeperInstance> serializer) {
		this.serviceDiscovery = buildServiceDiscoveryWith(configuration, curator, serializer);
		this.curator = curator;
	}

	private CuratorFramework buildCuratorWith(ZookeeperConfiguration configuration) {
		return CuratorFrameworkFactory.builder()
				.connectString(configuration.connectionString())
				.retryPolicy(new RetryNever())
					.build();
	}

	private ServiceDiscovery<ZookeeperInstance> buildServiceDiscoveryWith(ZookeeperConfiguration configuration, CuratorFramework curator,
			InstanceSerializer<ZookeeperInstance> serializer) {

		try {
			if (!CuratorFrameworkState.STARTED.equals(curator.getState())) {
				curator.start();
			}

			ServiceDiscovery<ZookeeperInstance> serviceDiscovery = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
					.client(curator)
						.basePath(configuration.root())
						.serializer(serializer)
							.build();

			serviceDiscovery.start();

			return serviceDiscovery;
		} catch (Exception e) {
			throw new ZookeeperServiceDiscoveryException("Error on create Zookeeper ServiceDiscovery", e);
		}
	}

	public Collection<ServiceInstance<ZookeeperInstance>> queryForInstances(String serviceName) {
		return queryByName(serviceName);
	}

	private Collection<ServiceInstance<ZookeeperInstance>> queryByName(String serviceName) {
		return Tryable.of(() -> serviceDiscovery.queryForInstances(serviceName));
	}

	public void register(ZookeeperInstance zookeeperInstance) {
		try {
			ServiceInstanceBuilder<ZookeeperInstance> builder = ServiceInstance.builder();

			ServiceInstance<ZookeeperInstance> instance = builder.name(zookeeperInstance.name())
				.payload(zookeeperInstance)
				.address(zookeeperInstance.address())
				.port(zookeeperInstance.port())
				.uriSpec(new UriSpec("{scheme}://{address}:{port}"))
				.build();

			serviceDiscovery.registerService(instance);

		} catch (Exception e) {
			throw new ZookeeperServiceDiscoveryException("Exception on register Zookeeper service instance [" + zookeeperInstance + "]" , e);
		}
	}

	public void unregister(ZookeeperInstance zookeeperInstance) {
		try {
			queryByExample(zookeeperInstance)
				.ifPresent(instance -> Tryable.run(() -> serviceDiscovery.unregisterService(instance)));

		} catch (Exception e) {
			throw new ZookeeperServiceDiscoveryException("Exception on unregister Zookeeper service instance [" + zookeeperInstance + "]" , e);
		}
	}

	public Optional<ServiceInstance<ZookeeperInstance>> queryByExample(ZookeeperInstance zookeeperInstance) {
		return Tryable.of(() -> serviceDiscovery.queryForInstances(zookeeperInstance.name()))
				.stream()
					.filter(i -> i.getAddress().equals(zookeeperInstance.address()) && i.getPort().equals(zookeeperInstance.port()))
						.findFirst();
	}

	public void close() {
		Tryable.run(serviceDiscovery::close);
		Tryable.run(curator::close);
	}

	private class RetryNever implements RetryPolicy {

		@Override
		public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
			return false;
		}

	}
}
