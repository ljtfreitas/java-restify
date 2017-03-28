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

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZookeeperInstance {

	@JsonProperty
	private String id;

	@JsonProperty
	private String name;

	@JsonProperty
	private int port;

	@JsonProperty
	private String address;

	@JsonProperty
	private Map<String, String> metadata;

	@Deprecated
	ZookeeperInstance() {
	}

	public ZookeeperInstance(String name, String address, int port) {
		this(name, address, port, Collections.emptyMap());
	}

	public ZookeeperInstance(String name, String address, int port, Map<String, String> metadata) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.address = address;
		this.port = port;
		this.metadata = metadata;
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public int port() {
		return port;
	}

	public String address() {
		return address;
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
				.append("Metadata: ")
					.append(metadata)
			.append("]");

		return report.toString();
	}
}
