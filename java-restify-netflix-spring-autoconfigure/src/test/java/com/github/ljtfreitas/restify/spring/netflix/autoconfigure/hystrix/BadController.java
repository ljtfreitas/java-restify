package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bad")
public class BadController {

	@GetMapping("/get")
	public void get() {
		throw new RuntimeException("Internal server error...");
	}
}