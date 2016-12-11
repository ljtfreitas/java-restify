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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;

import com.github.ljtfreitas.restify.http.util.Tryable;

public class ZookeeperServiceDiscovery {

	private final ServiceDiscovery<ZookeeperInstance> serviceDiscovery;

	public ZookeeperServiceDiscovery(ZookeeperDiscoveryConfiguration configuration, CuratorFramework curator,
			InstanceSerializer<ZookeeperInstance> serializer) {
		this.serviceDiscovery = buildServiceDiscoveryWith(configuration, curator, serializer);
	}

	private ServiceDiscovery<ZookeeperInstance> buildServiceDiscoveryWith(ZookeeperDiscoveryConfiguration configuration, CuratorFramework curator,
			InstanceSerializer<ZookeeperInstance> serializer) {

		ServiceDiscovery<ZookeeperInstance> serviceDiscovery = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
					.client(curator)
						.basePath(configuration.root())
						.serializer(serializer)
							.build();

		try {
			serviceDiscovery.start();

			return serviceDiscovery;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<ServiceInstance<ZookeeperInstance>> queryForInstances(String serviceName) {
		return Tryable.of(() -> serviceDiscovery.queryForInstances(serviceName));
	}

	public void register(ZookeeperInstance zookeeperInstance) {
		try {
			ServiceInstanceBuilder<ZookeeperInstance> builder = ServiceInstance.builder();

			ServiceInstance<ZookeeperInstance> instance = builder.name(zookeeperInstance.name())
				.payload(zookeeperInstance)
				.port(zookeeperInstance.port())
				.address(zookeeperInstance.address())
				.uriSpec(new UriSpec("{scheme}://{address}:{port}"))
				.build();

			serviceDiscovery.registerService(instance);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		Tryable.run(serviceDiscovery::close);
	}
}
