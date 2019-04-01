/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.spring.configure;

import java.util.Collection;

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

	RestifyProxyBeanBuilder asyncExecutorServiceName(String executorServiceName) {
		builder.addPropertyReference("asyncExecutorService", executorServiceName);
		return this;
	}

	RestifyProxyBeanBuilder configurations(Collection<RestifyProxyConfiguration> configurations) {
		builder.addPropertyValue("configurations", configurations);
		return this;
	}

	public BeanDefinition build() {
		BeanDefinition beanDefinition = doBuild();
		beanDefinition.setPrimary(true);
		return beanDefinition;
	}

	private BeanDefinition doBuild() {
		return builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
					.setLazyInit(true)
						.getBeanDefinition();
	}

}
