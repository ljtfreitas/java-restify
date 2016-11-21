package com.restify.spring.configure;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;

import com.restify.http.client.authentication.Authentication;
import com.restify.http.client.authentication.BasicAuthentication;
import com.restify.spring.configure.RestifyApiClient.Basic;
import com.restify.spring.configure.RestifyApiClient.RestifyApiAuthentication;

public abstract class BaseRestifyConfigurationRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

	private static final Logger log = LoggerFactory.getLogger(RestifyConfigurationRegistrar.class);

	private RestifyProperties restifyProperties;

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
		RestifyApiClient restifyApiClient = restifyProperties.client(type);

		String endpoint = type.endpoint().map(e -> resolve(e)).orElseGet(restifyApiClient::getEndpoint);

		RestifyProxyBeanBuilder builder = new RestifyProxyBeanBuilder()
				.objectType(type.objectType())
					.endpoint(endpoint)
						.asyncExecutorServiceName("restifyAsyncExecutorService")
							.authentication(apiAuthentication(restifyApiClient.getAuthentication()));

		registry.registerBeanDefinition(type.name(), builder.build());

		log.info("Create @Restifyable bean -> {} (API [{}] metadata: Description: [{}], and endpoint: [{}])",
				type.objectType(), type.name(), type.description(), endpoint);
	}

	private String resolve(String expression) {
		return Optional.ofNullable(restifyProperties.resolve(expression))
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
		this.restifyProperties = new RestifyProperties(environment);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

}
