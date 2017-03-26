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

import org.springframework.boot.bind.PropertySourcesPropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

class RestifyApiClientProperties {

	private final Environment environment;

	RestifyApiClientProperties(Environment environment) {
		this.environment = environment;
	}

	public RestifyApiClient client(RestifyableType type) {
		RestifyApiClient restifyApiClient = new RestifyApiClient();

		RelaxedDataBinder dataBinder = new RelaxedDataBinder(restifyApiClient, "restify." + type.name());

		ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
		dataBinder.bind(new PropertySourcesPropertyValues(configurableEnvironment.getPropertySources()));

		return restifyApiClient;
	}

	public String resolve(String expression) {
		return ((ConfigurableEnvironment) environment).resolvePlaceholders(expression);
	}
}
