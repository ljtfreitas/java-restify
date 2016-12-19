package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

	@GetMapping("/get")
	public String get() {
		return "Ribbon it's works!";
	}
}