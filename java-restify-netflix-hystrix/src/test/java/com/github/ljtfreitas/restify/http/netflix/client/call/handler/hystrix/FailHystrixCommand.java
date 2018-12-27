package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

class FailHystrixCommand extends HystrixCommand<String> {

	private final String fallback;
	
	FailHystrixCommand(String fallback) {
		super(HystrixCommandGroupKey.Factory.asKey("fail"));
		this.fallback = fallback;
	}

	@Override
	protected String run() throws Exception {
		throw new RuntimeException("ooops");
	}

	@Override
	protected String getFallback() {
		return fallback;
	}
}