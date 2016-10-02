package com.restify.http.spring.autoconfigure;

import java.net.URL;

class RestifyProxyTarget {

	private final Class<?> objectType;
	private final URL endpoint;

	RestifyProxyTarget(Class<?> objectType, URL endpoint) {
		this.objectType = objectType;
		this.endpoint = endpoint;
	}

	public Class<?> objectType() {
		return objectType;
	}

	public URL endpoint() {
		return endpoint;
	}
}
