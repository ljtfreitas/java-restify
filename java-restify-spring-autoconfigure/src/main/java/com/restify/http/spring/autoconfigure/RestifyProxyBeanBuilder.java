package com.restify.http.spring.autoconfigure;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

class RestifyProxyBeanBuilder {

	private final BeanDefinitionBuilder builder;

	RestifyProxyBeanBuilder() {
		this.builder = BeanDefinitionBuilder.genericBeanDefinition(RestifyProxyFactoryBean.class);
	}

	RestifyProxyBeanBuilder objectType(Class<?> objectType) {
		builder.addPropertyValue("objectType", objectType);
		return this;
	}

	RestifyProxyBeanBuilder endpoint(String endpoint) {
		builder.addPropertyValue("endpoint", endpoint);
		return this;
	}

	BeanDefinition build() {
		builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		return builder.getBeanDefinition();
	}
}
