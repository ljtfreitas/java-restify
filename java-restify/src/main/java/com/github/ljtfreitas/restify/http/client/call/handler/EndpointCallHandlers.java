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
package com.github.ljtfreitas.restify.http.client.call.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;

public class EndpointCallHandlers {

	private static final EndpointCallHandlerFactory<? super Object, ? super Object> DEFAULT_HANDLER_FACTORY
		= new DefaultEndpointCallHandlerFactory<>();

	private final Collection<EndpointCallHandlerProvider> providers;

	public EndpointCallHandlers(Collection<EndpointCallHandlerProvider> providers) {
		this.providers = new ArrayList<>(providers);
	}

	public <M, T> EndpointCallHandler<M, T> of(EndpointMethod endpointMethod) {
		return search(endpointMethod, new Exclusions());
	}

	private <M, T> EndpointCallHandler<M, T> search(EndpointMethod endpointMethod, Exclusions exclusions) {
		EndpointCallHandlerProvider provider = doSearch(endpointMethod, exclusions);
		return adapter(provider) ? adapt(endpointMethod, provider, exclusions) : create(endpointMethod, provider);
	}

	private EndpointCallHandlerProvider doSearch(EndpointMethod endpointMethod, Exclusions exclusions) {
		return providers.stream()
				.filter(f -> (exclusions == null || exclusions.empty()) || (!exclusions.contains(f)))
					.filter(f -> f.supports(endpointMethod))
						.findFirst()
							.orElseGet(() -> DEFAULT_HANDLER_FACTORY);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <M, T> EndpointCallHandler<M, T> create(EndpointMethod endpointMethod, EndpointCallHandlerProvider provider) {
		EndpointCallHandlerFactory<M, T> factory = (EndpointCallHandlerFactory) provider;
		return factory.create(endpointMethod);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <M, T, O> EndpointCallHandler<M, O> adapt(EndpointMethod endpointMethod, EndpointCallHandlerProvider provider, Exclusions exclusions) {
		EndpointCallHandlerAdapter<M, T, O> adapter = (EndpointCallHandlerAdapter) provider;
		return adapter.adapt(endpointMethod, search(endpointMethod.returns(adapter.returnType(endpointMethod)), exclusions.add(adapter)));
	}

	private boolean adapter(EndpointCallHandlerProvider provider) {
		return provider instanceof EndpointCallHandlerAdapter;
	}
	
	private class Exclusions {
		
		private final Collection<Class<? extends EndpointCallHandlerProvider>> values = new HashSet<>();
		
		private Exclusions add(EndpointCallHandlerProvider provider) {
			this.values.add(provider.getClass());
			return this;
		}

		private boolean contains(EndpointCallHandlerProvider provider) {
			return values.contains(provider.getClass());
		}

		private boolean empty() {
			return values.isEmpty();
		}
	}
}
