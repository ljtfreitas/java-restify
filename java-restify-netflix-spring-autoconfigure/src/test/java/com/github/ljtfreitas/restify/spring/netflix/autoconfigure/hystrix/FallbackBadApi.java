package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.stereotype.Service;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;

@Service
public class FallbackBadApi {

	public String get() {
		return "this is BadApi fallback!";
	}

	public HystrixCommand<String> getAsHystrixCommand() {
		Setter hystrixMetadata = HystrixCommand.Setter
				.withGroupKey(HystrixCommandGroupKey.Factory.asKey("FallbackUnknownApi"));

		return new HystrixCommand<String>(hystrixMetadata) {
			@Override
			protected String run() throws Exception {
				return "this is BadApi (command) fallback!";
			}
		};
	}
}
