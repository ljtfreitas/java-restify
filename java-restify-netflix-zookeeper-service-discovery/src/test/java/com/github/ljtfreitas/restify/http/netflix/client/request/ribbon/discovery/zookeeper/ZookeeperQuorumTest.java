package com.github.ljtfreitas.restify.http.netflix.client.request.ribbon.discovery.zookeeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.netflix.client.request.ribbon.discovery.zookeeper.ZookeeperQuorum;

public class ZookeeperQuorumTest {

	@Test
	public void shouldBuildZookeeperQuorumFromConnectionString() {
		String connectionString = "localhost:2181,localhost:2182";

		ZookeeperQuorum quorum = ZookeeperQuorum.of(connectionString);

		assertEquals(connectionString, quorum.connectionString());
		assertNull(quorum.chroot());
	}

	@Test
	public void shouldBuildZookeeperQuorumFromConnectionStringWithChroot() {
		String connectionString = "localhost:2181,localhost:2182/services";

		ZookeeperQuorum quorum = ZookeeperQuorum.of(connectionString);

		assertEquals("localhost:2181,localhost:2182", quorum.connectionString());
		assertEquals("/services", quorum.chroot());
	}
}
