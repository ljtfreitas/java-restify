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
package com.github.ljtfreitas.restify.http.netflix.client.request.discovery.kubernetes;

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.util.Tryable;

import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointPort;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.KubernetesClient;

public class DefaultKubernetesServiceDiscovery implements KubernetesServiceDiscovery {

	private final KubernetesDiscoveryConfiguration configuration;
	private final KubernetesClient kubernetesClient;

	public DefaultKubernetesServiceDiscovery(KubernetesClient kubernetesClient) {
		this(kubernetesClient, new KubernetesDiscoveryConfiguration());
	}

	public DefaultKubernetesServiceDiscovery(KubernetesClient kubernetesClient, KubernetesDiscoveryConfiguration configuration) {
		this.kubernetesClient = kubernetesClient;
		this.configuration = configuration;
	}

	@Override
	public Collection<KubernetesServiceInstance> queryForInstances(String serviceName) {
		nonNull(serviceName, "Service name must be provided!");
		return findEndpoints(serviceName).getSubsets().stream()
			.flatMap(subset -> subset.getPorts().stream()
				.filter(port -> !configuration.portName().isPresent() || configuration.portName().get().endsWith(port.getName()))
				.flatMap(port -> subset.getAddresses().stream()
					.map(address -> convert(serviceName, address, port))))
						.collect(Collectors.toList());
	}

	private KubernetesServiceInstance convert(String serviceName, EndpointAddress address, EndpointPort port) {
		return new KubernetesServiceInstance(serviceName, address, port);
	}

	private Endpoints findEndpoints(String serviceName) {
		Endpoints endpoints = configuration.namespace().isPresent() ?
				kubernetesClient.endpoints().inNamespace(configuration.namespace().get()).withName(serviceName).get() :
				kubernetesClient.endpoints().withName(serviceName).get();

		return Optional.ofNullable(endpoints).orElse(new Endpoints());
	}

	@Override
	public void close() throws IOException {
		Tryable.silently(kubernetesClient::close);
	}

}
