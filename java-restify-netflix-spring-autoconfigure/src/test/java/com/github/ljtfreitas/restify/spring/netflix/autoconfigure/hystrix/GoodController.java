package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/good")
public class GoodController {

	@GetMapping("/get")
	public String get() {
		return "It's works!";
	}
}