package com.github.ljtfreitas.restify.http.client.call.handler.vavr;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;

public class VavrEndpointCallHandlerProviderSpiTest {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDiscoveryServiceProviders() {
		Iterable<EndpointCallHandlerProvider> services = ServiceLoader.load(EndpointCallHandlerProvider.class);

		assertThat(services, contains(
				instanceOf(ArrayEndpointCallHandlerAdapter.class),
				instanceOf(EitherWithStringEndpointCallHandlerAdapter.class),
				instanceOf(EitherWithThrowableEndpointCallHandlerAdapter.class),
				instanceOf(FutureEndpointCallHandlerAdapter.class),
				instanceOf(IndexedSeqEndpointCallHandlerAdapter.class),
				instanceOf(LazyEndpointCallHandlerAdapter.class),
				instanceOf(ListEndpointCallHandlerAdapter.class),
				instanceOf(OptionEndpointCallHandlerFactory.class),
				instanceOf(QueueEndpointCallHandlerAdapter.class),
				instanceOf(SeqEndpointCallHandlerAdapter.class),
				instanceOf(SetEndpointCallHandlerAdapter.class),
				instanceOf(TraversableEndpointCallHandlerAdapter.class),
				instanceOf(TryEndpointCallHandlerAdapter.class)));
	}
}
