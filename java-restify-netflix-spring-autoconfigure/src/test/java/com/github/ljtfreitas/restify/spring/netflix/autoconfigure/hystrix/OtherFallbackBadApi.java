package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.stereotype.Service;

@Service
public class OtherFallbackBadApi {

	public String get() {
		return "this is BadApi fallback!";
	}
}
