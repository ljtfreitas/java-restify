package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.OnCircuitBreaker;
import com.github.ljtfreitas.restify.spring.configure.Restifyable;
import com.netflix.hystrix.HystrixCommand;

@Restifyable(endpoint = "http://my-api")
public interface BadApi {

	@OnCircuitBreaker
	@RequestMapping(path = "/bad", method = RequestMethod.GET)
	public String get();

	@RequestMapping(path = "/bad", method = RequestMethod.GET)
	public HystrixCommand<String> getAsHystrixCommand();
}
