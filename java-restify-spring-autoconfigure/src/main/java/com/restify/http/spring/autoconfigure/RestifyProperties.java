package com.restify.http.spring.autoconfigure;

import org.springframework.boot.bind.PropertySourcesPropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

class RestifyProperties {

	private final Environment environment;

	RestifyProperties(Environment environment) {
		this.environment = environment;
	}

	RestifyApiClient client(RestifyableType type) {
		RestifyApiClient restifyApiClient = new RestifyApiClient();

		RelaxedDataBinder dataBinder = new RelaxedDataBinder(restifyApiClient, "restify." + type.name());

		ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
		dataBinder.bind(new PropertySourcesPropertyValues(configurableEnvironment.getPropertySources()));

		return restifyApiClient;
	}

	String resolve(String expression) {
		return ((ConfigurableEnvironment) environment).resolvePlaceholders(expression);
	}

	class RestifyApiClient {

		private String endpoint;

		public String getEndpoint() {
			return endpoint;
		}

		public void setEndpoint(String endpoint) {
			this.endpoint = endpoint;
		}
	}
}
