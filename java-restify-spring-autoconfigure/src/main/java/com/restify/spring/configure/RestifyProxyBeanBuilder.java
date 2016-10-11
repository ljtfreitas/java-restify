package com.restify.spring.configure;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class RestifyProxyBeanBuilder {

	private final BeanDefinitionBuilder builder;

	public RestifyProxyBeanBuilder() {
		this.builder = BeanDefinitionBuilder.genericBeanDefinition(RestifyProxyFactoryBean.class);
	}

	public RestifyProxyBeanBuilder objectType(Class<?> objectType) {
		builder.addPropertyValue("objectType", objectType);
		return this;
	}

	public RestifyProxyBeanBuilder endpoint(String endpoint) {
		builder.addPropertyValue("endpoint", endpoint);
		return this;
	}

	public BeanDefinition build() {
		builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		return builder.getBeanDefinition();
	}
}
