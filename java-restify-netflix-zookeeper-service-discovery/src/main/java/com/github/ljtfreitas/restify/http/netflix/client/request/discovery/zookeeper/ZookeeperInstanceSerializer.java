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

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.ljtfreitas.restify.util.Tryable;

public class ZookeeperInstanceSerializer implements InstanceSerializer<ZookeeperServiceInstance> {

	private final ObjectMapper mapper;

	public ZookeeperInstanceSerializer() {
		this(new ObjectMapper());
	}

	public ZookeeperInstanceSerializer(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public ServiceInstance<ZookeeperServiceInstance> deserialize(byte[] bytes) throws Exception {
		JsonNode tree = mapper.readTree(bytes);

		String id = tree.get("id").asText();
		String name = tree.get("name").asText();
		String address = tree.get("address").asText();

		Integer port = Optional.ofNullable(tree.get("port"))
				.map(p -> p.asInt())
					.orElse(null);

		Integer sslPort = Optional.ofNullable(tree.get("sslPort"))
				.map(p -> p.asInt())
					.orElse(null);

		long registrationTimeUTC = Optional.ofNullable(tree.get("registrationTimeUTC"))
				.map(r -> r.asLong())
					.orElse(0l);

		ServiceType serviceType = Optional.ofNullable(tree.get("serviceType"))
				.map(s -> ServiceType.valueOf(s.asText()))
					.orElse(null);

		UriSpec uriSpec = Optional.ofNullable(tree.get("uriSpec"))
				.map(u -> new UriSpec(u.asText()))
					.orElse(null);

		JsonNode payload = tree.get("payload");

		ZookeeperServiceInstance zookeeperInstance = Optional.ofNullable(payload)
				.map(p -> Tryable.of(() -> mapper.readValue(p, ZookeeperServiceInstance.class)))
					.orElse(null);

		return new ServiceInstance<>(name, id, address, port, sslPort, zookeeperInstance, registrationTimeUTC,
				serviceType, uriSpec);
	}

	@Override
	public byte[] serialize(ServiceInstance<ZookeeperServiceInstance> instance) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mapper.writeValue(out, instance);
		return out.toByteArray();
	}

}
