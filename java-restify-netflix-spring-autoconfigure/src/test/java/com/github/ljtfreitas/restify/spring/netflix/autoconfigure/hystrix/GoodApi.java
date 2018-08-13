package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

<<<<<<< HEAD
import com.github.ljtfreitas.restify.http.netflix.client.call.hystrix.OnCircuitBreaker;
=======
import com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix.OnCircuitBreaker;
>>>>>>> ea4d3f4... Mudança de nomes
import com.github.ljtfreitas.restify.spring.configure.Restifyable;
import com.netflix.hystrix.HystrixCommand;

@Restifyable(endpoint = "http://my-api")
public interface GoodApi {

	@OnCircuitBreaker
	@RequestMapping(path = "/good", method = RequestMethod.GET)
	public String get();

	@RequestMapping(path = "/good", method = RequestMethod.GET)
	public HystrixCommand<String> getAsHystrixCommand();
}
