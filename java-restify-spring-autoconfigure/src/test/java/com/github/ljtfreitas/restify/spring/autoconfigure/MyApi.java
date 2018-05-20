package com.github.ljtfreitas.restify.spring.autoconfigure;

import org.springframework.web.bind.annotation.GetMapping;

import com.github.ljtfreitas.restify.spring.configure.Restifyable;

@Restifyable(name = "my-api", description = "My Api - Enjoy!")
public interface MyApi {

	@GetMapping("/sample")
	String sample();
}