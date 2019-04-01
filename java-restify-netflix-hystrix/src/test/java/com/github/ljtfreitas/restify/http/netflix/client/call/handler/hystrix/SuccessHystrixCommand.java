package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

class SuccessHystrixCommand extends HystrixCommand<String> {

	private final String result;

	public SuccessHystrixCommand(String result) {
		super(HystrixCommandGroupKey.Factory.asKey("success"));
		this.result = result;
	}

	@Override
	protected String run() throws Exception {
		return result;
	}
}