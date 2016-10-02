package com.restify.http.spring.autoconfigure;

import java.net.URL;
import java.util.Optional;

import org.springframework.beans.factory.FactoryBean;

import com.restify.http.RestifyProxyBuilder;

class RestifyProxyFactoryBean implements FactoryBean<Object> {

	private final RestifyProxyTarget target;
	private final RestifyProxyBuilder restifyProxyBuilder;

	RestifyProxyFactoryBean(RestifyProxyTarget target, RestifyProxyBuilder restifyProxyBuilder) {
		this.target = target;
		this.restifyProxyBuilder = restifyProxyBuilder;
	}

	@Override
	public Object getObject() throws Exception {
		return restifyProxyBuilder
				.target(target.objectType(), Optional.ofNullable(target.endpoint()).map(URL::toString).orElse(null))
				.build();
	}

	@Override
	public Class<?> getObjectType() {
		return target.objectType();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
