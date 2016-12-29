package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.OnCircuitBreaker;
import com.github.ljtfreitas.restify.spring.configure.Restifyable;
import com.netflix.hystrix.HystrixCommand;

@Restifyable(endpoint = "http://localhost:8080/bad")
public interface BadApi {

	@OnCircuitBreaker
	@RequestMapping(path = "/get", method = RequestMethod.GET)
	public String get();

	@RequestMapping(path = "/get", method = RequestMethod.GET)
	public HystrixCommand<String> getAsHystrixCommand();
}
