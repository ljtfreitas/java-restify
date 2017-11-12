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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class ZookeeperQuorum {

	private static final int DEFAULT_PORT = 2181;

	private final Collection<ZookeeperServer> servers;
	private final String chroot;

	private ZookeeperQuorum(Collection<ZookeeperServer> servers, String chroot) {
		this.servers = servers;
		this.chroot = chroot;
	}

	public String chroot() {
		return chroot;
	}

	public String connectionString() {
		return doConnectionString();
	}

	@Override
	public String toString() {
		return doConnectionString();
	}

	private String doConnectionString() {
		return servers.stream().map(ZookeeperServer::toString).collect(Collectors.joining(","));
	}

	public static ZookeeperQuorum of(String connectionString) {
		String chroot = null;

		int slash = connectionString.lastIndexOf("/");
		if (slash >= 0) {
			chroot = connectionString.substring(slash);
		}

		String hosts = connectionString.substring(0, slash == -1 ? connectionString.length() : slash);

		Collection<ZookeeperServer> servers = Arrays.stream(hosts.split(","))
			.map(host -> host.split(":"))
				.map(host -> new ZookeeperServer(host[0], host.length == 1 ? DEFAULT_PORT : Integer.valueOf(host[1])))
					.collect(Collectors.toList());

		return new ZookeeperQuorum(servers, chroot);
	}

	private static class ZookeeperServer {

		private final String address;
		private final int port;

		private ZookeeperServer(String address, int port) {
			this.address = nonNull(address, "Zookeeper address cannot be null");
			this.port = port;
		}

		@Override
		public String toString() {
			return address + ":" + port;
		}
	}
}
