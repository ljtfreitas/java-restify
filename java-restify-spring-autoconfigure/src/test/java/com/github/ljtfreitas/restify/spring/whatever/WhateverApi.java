package com.github.ljtfreitas.restify.spring.whatever;

import org.springframework.web.bind.annotation.GetMapping;

import com.github.ljtfreitas.restify.spring.configure.Restifyable;

@Restifyable(description = "Whatever Api - Enjoy!", endpoint = "http://localhost:8080")
public interface WhateverApi {

	@GetMapping("/whatever")
	String sample();

}