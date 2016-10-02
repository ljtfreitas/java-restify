package com.restify.http.spring.autoconfigure;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.net.URL;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import com.restify.http.RestifyProxyBuilder;
import com.restify.http.client.authentication.Authentication;
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
@AutoConfigureAfter(WebClientAutoConfiguration.class)
public class RestifyAutoConfiguration {

	@Autowired
	private EndpointRequestExecutor endpointRequestExecutor;

	@Autowired
	private RestifyContractReader restifyContractReader;

	@Autowired
	private Authentication authentication;

	@ConditionalOnMissingBean
	@Scope(SCOPE_PROTOTYPE)
	@Bean
	public RestifyProxyBuilder restifyProxyBuilder() {
		return new RestifyProxyBuilder()
				.authentication(authentication)
				.executor(endpointRequestExecutor)
				.contract(restifyContractReader);
	}

	@Configuration
	protected static class RestifyConfiguration {

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

		@ConditionalOnMissingBean
		@Bean
		public Authentication authentication() {
			return () -> "";
		}
	}

	static class RestifyAutoConfigurationRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

		private static final Logger log = LoggerFactory.getLogger(RestifyAutoConfigurationRegistrar.class);

		private BeanFactory beanFactory;

		private RestifyProperties restifyProperties;

		private RestifyableTypeScanner scanner = new RestifyableTypeScanner();

		@Override
		public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
			AutoConfigurationPackages.get(beanFactory).forEach(p -> scan(p, registry));
		}

		private void scan(String packageName, BeanDefinitionRegistry registry) {
			scanner.findCandidateComponents(packageName)
				.stream()
					.map(c -> new RestifyableType(c.getBeanClassName()))
						.forEach(t -> create(t, registry));
		}

		private void create(RestifyableType restifyableType, BeanDefinitionRegistry registry) {
			RestifyApiClient restifyApiClient = restifyProperties.client(restifyableType);

			URL endpoint = restifyableType.endpoint().orElseGet(restifyApiClient::getEndpoint);

			RestifyProxyBeanBuilder builder = new RestifyProxyBeanBuilder()
					.target(new RestifyProxyTarget(restifyableType.objectType(), endpoint))
						.proxyBuilderBeanName(name(RestifyProxyBuilder.class).orElse("restifyProxyBuilder"));

			registry.registerBeanDefinition(restifyableType.name(), builder.build());

			log.info("Create @Restifyable bean -> {} (API [{}] metadata: Description: [{}], and endpoint: [{}])",
					restifyableType.objectType(), restifyableType.name(), restifyableType.description(), endpoint);
		}

		private Optional<String> name(Class<?> beanType) {
			String[] names = ((ListableBeanFactory) beanFactory).getBeanNamesForType(beanType);
			return Optional.ofNullable(names.length == 0 ? null : names[0]);
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
}
