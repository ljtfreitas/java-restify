package com.restify.http.spring.autoconfigure;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

class RestifyProxyBeanBuilder {

	private final BeanDefinitionBuilder builder;

	RestifyProxyBeanBuilder() {
		this.builder = BeanDefinitionBuilder.genericBeanDefinition(RestifyProxyFactoryBean.class);
	}

	RestifyProxyBeanBuilder target(RestifyProxyTarget target) {
		builder.addConstructorArgValue(target);
		return this;
	}

	RestifyProxyBeanBuilder proxyBuilderBeanName(String proxyBuilderBeanName) {
		builder.addConstructorArgReference(proxyBuilderBeanName);
		return this;
	}

	BeanDefinition build() {
		return builder.getBeanDefinition();
	}
}
