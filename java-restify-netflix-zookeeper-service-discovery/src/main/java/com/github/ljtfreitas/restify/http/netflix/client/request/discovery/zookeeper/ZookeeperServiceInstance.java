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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.ServiceInstance;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZookeeperServiceInstance implements ServiceInstance {

	@JsonProperty
	private String id;

	@JsonProperty
	private String name;

	@JsonProperty
	private int port;

	@JsonProperty
	private Integer sslPort;

	@JsonProperty
	private String scheme;

	@JsonProperty
	private String address;

	@JsonProperty
	private Map<String, String> metadata;

	@Deprecated
	ZookeeperServiceInstance() {
	}

	public ZookeeperServiceInstance(ServiceInstance source) {
		this(source.name(), source.host(), source.port());
	}

	public ZookeeperServiceInstance(String name, String address, int port) {
		this(name, address, port, Collections.emptyMap());
	}

	public ZookeeperServiceInstance(String name, String address, int port, Map<String, String> metadata) {
		this(UUID.randomUUID().toString(), name, address, port, metadata);
	}

	public ZookeeperServiceInstance(String id, String name, String address, int port, Map<String, String> metadata) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.port = port;
		this.scheme = "http";
		this.metadata = metadata;
	}

	public String id() {
		return id;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int port() {
		return port;
	}

	@Override
	public String host() {
		return address;
	}

	public Optional<Integer> sslPort() {
		return Optional.ofNullable(sslPort);
	}

	public void sslPort(Integer sslPort) {
		this.sslPort = sslPort;
	}

	public String scheme() {
		return scheme;
	}

	public void scheme(String scheme) {
		this.scheme = scheme;
	}

	public Map<String, String> metadata() {
		return Collections.unmodifiableMap(metadata);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("ZookeeperInstance: [")
				.append("Id: ")
					.append(id)
				.append(", ")
				.append("Name: ")
					.append(name)
				.append(", ")
				.append("Address: ")
					.append(address)
				.append(", ")
				.append("Port: ")
					.append(port)
				.append(", ")
				.append("SSL Port: ")
					.append(sslPort)
				.append(", ")
				.append("Scheme: ")
					.append(scheme)
				.append(", ")
				.append("Metadata: ")
					.append(metadata)
			.append("]");

		return report.toString();
	}

	public static ZookeeperServiceInstance of(org.apache.curator.x.discovery.ServiceInstance<?> source) {
		ZookeeperServiceInstance zookeeperServiceInstance = new ZookeeperServiceInstance(source.getId(),
				source.getName(), source.getAddress(), source.getPort(), Collections.emptyMap());

		zookeeperServiceInstance.scheme(source.getSslPort() == null ? "http" : "https");
		zookeeperServiceInstance.sslPort(source.getSslPort());

		return zookeeperServiceInstance;
	}
}
