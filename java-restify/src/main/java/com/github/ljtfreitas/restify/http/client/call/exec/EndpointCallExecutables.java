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
package com.github.ljtfreitas.restify.http.client.call.exec;

import java.util.ArrayList;
import java.util.Collection;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;

public class EndpointCallExecutables {

	private static final EndpointCallExecutableFactory<? super Object, ? super Object> DEFAULT_EXECUTABLE_FACTORY
		= new DefaultEndpointCallExecutableFactory<>();

	private final Collection<EndpointCallExecutableProvider> providers;

	public EndpointCallExecutables(Collection<EndpointCallExecutableProvider> providers) {
		this.providers = new ArrayList<>(providers);
	}

	public <M, T> EndpointCallExecutable<M, T> of(EndpointMethod endpointMethod) {
		EndpointCallExecutableProvider provider = providerTo(endpointMethod);
		return decorator(provider) ? decorate(endpointMethod, provider) : create(endpointMethod, provider);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <M, T> EndpointCallExecutable<M, T> create(EndpointMethod endpointMethod, EndpointCallExecutableProvider provider) {
		EndpointCallExecutableFactory<M, T> factory = (EndpointCallExecutableFactory) provider;
		return factory.create(endpointMethod);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <M, T, O> EndpointCallExecutable<M, O> decorate(EndpointMethod endpointMethod, EndpointCallExecutableProvider provider) {
		EndpointCallExecutableDecoratorFactory<M, T, O> decorator = (EndpointCallExecutableDecoratorFactory) provider;
		return decorator.create(endpointMethod, of(endpointMethod.with(decorator.returnType(endpointMethod))));
	}

	private boolean decorator(EndpointCallExecutableProvider provider) {
		return provider instanceof EndpointCallExecutableDecoratorFactory;
	}

	private EndpointCallExecutableProvider providerTo(EndpointMethod endpointMethod) {
		return providers.stream()
					.filter(f -> f.supports(endpointMethod))
						.findFirst()
							.orElseGet(() -> DEFAULT_EXECUTABLE_FACTORY);
	}
}
