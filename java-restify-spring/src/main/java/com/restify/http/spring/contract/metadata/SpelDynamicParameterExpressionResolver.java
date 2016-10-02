package com.restify.http.spring.contract.metadata;

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;

public class SpelDynamicParameterExpressionResolver implements SpringDynamicParameterExpressionResolver {

	private final ConfigurableBeanFactory beanFactory;
	private final BeanExpressionResolver resolver;
	private final BeanExpressionContext context;

	public SpelDynamicParameterExpressionResolver(ConfigurableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.resolver = beanFactory.getBeanExpressionResolver();
		this.context = new BeanExpressionContext(beanFactory, null);
	}

	@Override
	public String resolve(String expression) {
		if (bean(expression)) {
			return resolver.evaluate(expression, context).toString();
		} else {
			return beanFactory.resolveEmbeddedValue(expression);
		}
	}

	private boolean bean(String expression) {
		return expression.startsWith(StandardBeanExpressionResolver.DEFAULT_EXPRESSION_PREFIX)
				&& expression.endsWith(StandardBeanExpressionResolver.DEFAULT_EXPRESSION_SUFFIX);
	}
}
