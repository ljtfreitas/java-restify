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

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;

import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.request.authentication.BasicAuthentication;
import com.github.ljtfreitas.restify.spring.configure.RestifyApiClient.Basic;
import com.github.ljtfreitas.restify.spring.configure.RestifyApiClient.RestifyApiAuthentication;

public abstract class BaseRestifyConfigurationRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

	private static final Logger log = LoggerFactory.getLogger(RestifyConfigurationRegistrar.class);

	private RestifyApiClientProperties properties;

	protected BeanFactory beanFactory;

	protected void doScan(List<String> packages, RestifyableTypeScanner scanner, BeanDefinitionRegistry registry) {
		packages.forEach(p -> {
			scanner.findCandidateComponents(p)
				.stream()
					.map(candidate -> new RestifyableType(candidate.getBeanClassName()))
						.forEach(type -> create(type, registry));
		});
	}

	private void create(RestifyableType type, BeanDefinitionRegistry registry) {
		RestifyApiClient restifyApiClient = properties.client(type);

		String endpoint = type.endpoint().map(e -> resolve(e)).orElseGet(restifyApiClient::getEndpoint);

		RestifyProxyBeanBuilder builder = new RestifyProxyBeanBuilder()
				.objectType(type.objectType())
					.endpoint(endpoint)
						.asyncExecutorServiceName("restifyAsyncExecutorService")
							.authentication(apiAuthentication(restifyApiClient.getAuthentication()));

		BeanDefinition bean = builder.build();

		registry.registerBeanDefinition(type.name(), bean);

		log.info("Create @Restifyable bean -> {} (API [{}] metadata: Description: [{}], and endpoint: [{}])",
				type.objectType(), type.name(), type.description(), endpoint);
	}

	private String resolve(String expression) {
		return Optional.ofNullable(properties.resolve(expression))
				.orElseGet(() -> ((ConfigurableBeanFactory) beanFactory).resolveEmbeddedValue(expression));
	}

	private Authentication apiAuthentication(RestifyApiAuthentication restifyApiAuthentication) {
		if (restifyApiAuthentication != null) {
			Basic basic = restifyApiAuthentication.getBasic();
			if (basic != null) {
				return new BasicAuthentication(basic.getUsername(), basic.getPassword());
			}
		}
		return null;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.properties = new RestifyApiClientProperties(environment);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

}
