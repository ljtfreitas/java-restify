package com.github.ljtfreitas.restify.http.netflix.client.call.hystrix;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.netflix.hystrix.HystrixCommand;

class OnCircuitBreakerMethodPredicate {

	private final EndpointMethod endpointMethod;
	private final Object fallback;

	OnCircuitBreakerMethodPredicate(EndpointMethod endpointMethod) {
		this(endpointMethod, null);
	}
	
	OnCircuitBreakerMethodPredicate(EndpointMethod endpointMethod, Object fallback) {
		this.endpointMethod = endpointMethod;
		this.fallback = fallback;
	}

	boolean test() {
		return onCircuitBreaker(endpointMethod)
				&& !returnHystrixCommand(endpointMethod)
					&& (fallback == null || sameTypeOfFallback(endpointMethod.javaMethod().getDeclaringClass()));
	}

	private boolean sameTypeOfFallback(Class<?> classType) {
		return classType.isAssignableFrom(fallback.getClass());
	}

	private boolean onCircuitBreaker(EndpointMethod endpointMethod) {
		return endpointMethod.metadata().contains(OnCircuitBreaker.class);
	}

	private boolean returnHystrixCommand(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(HystrixCommand.class);
	}
}
