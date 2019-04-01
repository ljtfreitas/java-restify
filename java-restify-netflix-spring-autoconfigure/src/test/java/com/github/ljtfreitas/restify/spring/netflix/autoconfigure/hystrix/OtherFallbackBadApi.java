package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import org.springframework.stereotype.Service;

@Service
class OtherFallbackBadApi {

	public String get() {
		return "this is BadApi fallback!";
	}
}
