package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.ljtfreitas.restify.spring.configure.Restifyable;

@Restifyable(name = "my-api", endpoint = "http://my-api")
public interface LoadBalancedApi {

	@RequestMapping(path = "/get", method = RequestMethod.GET)
	public String get();
}
