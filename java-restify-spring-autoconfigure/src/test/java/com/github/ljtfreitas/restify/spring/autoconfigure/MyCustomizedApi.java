package com.github.ljtfreitas.restify.spring.autoconfigure;

import org.springframework.web.bind.annotation.GetMapping;

import com.github.ljtfreitas.restify.spring.configure.Restifyable;

@Restifyable(name = "my-customized-api", configuration = MyCustomizedApiConfiguration.class)
public interface MyCustomizedApi {

	@GetMapping("/sample-customized")
	String sample();
}