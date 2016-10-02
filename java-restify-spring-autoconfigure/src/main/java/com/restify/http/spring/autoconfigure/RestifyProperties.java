package com.restify.http.spring.autoconfigure;

import java.net.URL;

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

	class RestifyApiClient {

		private URL endpoint;

		public URL getEndpoint() {
			return endpoint;
		}

		public void setEndpoint(URL endpoint) {
			this.endpoint = endpoint;
		}
	}
}
