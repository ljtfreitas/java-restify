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

import static com.github.ljtfreitas.restify.http.util.Preconditions.nonNull;

import java.io.IOException;
import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;

public class ZookeeperServiceInstanceDiscovery
	implements ZookeeperServiceDiscovery<ZookeeperServiceInstance>, ZookeeperServiceRegister<ZookeeperServiceInstance> {

	private final ZookeeperServiceDiscovery<ZookeeperServiceInstance> serviceDiscovery;
	private final ZookeeperServiceRegister<ZookeeperServiceInstance> serviceRegister;

	public ZookeeperServiceInstanceDiscovery(ZookeeperConfiguration configuration) {
		this(new DefaultZookeeperServiceDiscovery<>(ZookeeperServiceInstance.class, configuration, new ZookeeperInstanceSerializer()),
			 new DefaultZookeeperServiceRegister<>(ZookeeperServiceInstance.class, configuration, new ZookeeperInstanceSerializer()));
	}

	public ZookeeperServiceInstanceDiscovery(ZookeeperConfiguration configuration, CuratorFramework curator) {
		this(new DefaultZookeeperServiceDiscovery<>(ZookeeperServiceInstance.class, configuration, new ZookeeperInstanceSerializer(), curator),
			 new DefaultZookeeperServiceRegister<>(ZookeeperServiceInstance.class, configuration, new ZookeeperInstanceSerializer(), curator));
	}

	public ZookeeperServiceInstanceDiscovery(ZookeeperCuratorServiceDiscovery<ZookeeperServiceInstance> zookeeperCuratorServiceDiscovery) {
		this(new DefaultZookeeperServiceDiscovery<>(zookeeperCuratorServiceDiscovery),
			 new DefaultZookeeperServiceRegister<>(zookeeperCuratorServiceDiscovery));
	}

	private ZookeeperServiceInstanceDiscovery(ZookeeperServiceDiscovery<ZookeeperServiceInstance> serviceDiscovery,
			ZookeeperServiceRegister<ZookeeperServiceInstance> serviceRegister) {
		this.serviceDiscovery = serviceDiscovery;
		this.serviceRegister = serviceRegister;
	}

	public void register(ZookeeperServiceInstance instance) {
		serviceRegister.register(instance, Payload.of(instance));
	}

	@Override
	public void register(ZookeeperServiceInstance instance, ZookeeperServiceRegister.Payload<ZookeeperServiceInstance> payload) {
		serviceRegister.register(instance, payload);
	}

	@Override
	public void unregister(ZookeeperServiceInstance instance) {
		serviceRegister.unregister(instance);
	}

	@Override
	public Collection<ServiceInstance<ZookeeperServiceInstance>> queryForInstances(String serviceName) {
		nonNull(serviceName, "Service name must be provided!");
		return serviceDiscovery.queryForInstances(serviceName);
	}

	@Override
	public void onFailure(ZookeeperServiceInstance instance) {
		serviceDiscovery.onFailure(instance);
	}

	@Override
	public void close() throws IOException {
		serviceDiscovery.close();
		serviceRegister.close();
	}
}
