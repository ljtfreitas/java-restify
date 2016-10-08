package com.restify.http.spring.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.contract.metadata.RestifyContractReader;
import com.restify.http.spring.autoconfigure.RestifyAutoConfiguration.RestifyAutoConfigurationRegistrar;
import com.restify.http.spring.autoconfigure.RestifyProperties.RestifyApiClient;
import com.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;
import com.restify.http.spring.contract.SpringMvcContractReader;
import com.restify.http.spring.contract.metadata.SpelDynamicParameterExpressionResolver;
import com.restify.http.spring.contract.metadata.SpringDynamicParameterExpressionResolver;

@Configuration
@Import(RestifyAutoConfigurationRegistrar.class)
@ConditionalOnMissingBean(RestifyProxyFactoryBean.class)
@AutoConfigureAfter(WebClientAutoConfiguration.class)
public class RestifyAutoConfiguration {

	@Configuration
	protected static class RestifySpringMvcConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public EndpointRequestExecutor endpointRequestExecutor(RestTemplateBuilder restTemplateBuilder) {
			return new RestOperationsEndpointRequestExecutor(restTemplateBuilder.build());
		}

		@ConditionalOnMissingBean
		@Bean
		public RestifyContractReader restifyContractReader(SpringDynamicParameterExpressionResolver expressionResolver) {
			return new SpringMvcContractReader(expressionResolver);
		}

		@ConditionalOnMissingBean
		@Bean
		public SpringDynamicParameterExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
			return new SpelDynamicParameterExpressionResolver(beanFactory);
		}
	}

	protected static class RestifyAutoConfigurationRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

		private static final Logger log = LoggerFactory.getLogger(RestifyAutoConfigurationRegistrar.class);

		private BeanFactory beanFactory;

		private RestifyProperties restifyProperties;

		private RestifyableTypeScanner scanner = new RestifyableTypeScanner();

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
			AutoConfigurationPackages.get(beanFactory).forEach(p -> scan(p, registry));
		}

		private void scan(String packageName, BeanDefinitionRegistry registry) {
			scanner.findCandidateComponents(packageName)
				.stream()
					.map(candidate -> new RestifyableType(candidate.getBeanClassName()))
						.forEach(type -> create(type, registry));
		}

		private void create(RestifyableType type, BeanDefinitionRegistry registry) {
			RestifyApiClient restifyApiClient = restifyProperties.client(type);

			String endpoint = type.endpoint().map(e -> resolve(e)).orElseGet(restifyApiClient::getEndpoint);

			RestifyProxyBeanBuilder builder = new RestifyProxyBeanBuilder()
					.objectType(type.objectType())
						.endpoint(endpoint);

			registry.registerBeanDefinition(type.name(), builder.build());

			log.info("Create @Restifyable bean -> {} (API [{}] metadata: Description: [{}], and endpoint: [{}])",
					type.objectType(), type.name(), type.description(), endpoint);
		}

		private String resolve(String expression) {
			return restifyProperties.resolve(expression);
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public void setEnvironment(Environment environment) {
			this.restifyProperties = new RestifyProperties(environment);
		}
	}
}
