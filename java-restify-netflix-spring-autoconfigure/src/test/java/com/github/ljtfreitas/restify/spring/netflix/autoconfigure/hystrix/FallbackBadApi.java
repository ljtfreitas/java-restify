package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.stereotype.Service;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;

@Service
@RestifyFallback
public class FallbackBadApi implements BadApi {

	@Override
	public String get() {
		return "this is BadApi fallback!";
	}

	@Override
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
