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
package com.github.ljtfreitas.restify.spring.autoconfigure;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.AnnotationMetadata;

import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfiguration.RestifyAutoConfigurationRegistrar;
import com.github.ljtfreitas.restify.spring.configure.BaseRestifyConfigurationRegistrar;
import com.github.ljtfreitas.restify.spring.configure.RestifyAsyncConfiguration;
import com.github.ljtfreitas.restify.spring.configure.RestifyConfigurationProperties;
import com.github.ljtfreitas.restify.spring.configure.RestifyDefaultConfiguration;
import com.github.ljtfreitas.restify.spring.configure.RestifyJaxRsConfiguration;
import com.github.ljtfreitas.restify.spring.configure.RestifyProxyFactoryBean;
import com.github.ljtfreitas.restify.spring.configure.RestifySpringWebConfiguration;
import com.github.ljtfreitas.restify.spring.configure.RestifyableTypeScanner;

@Configuration
@EnableConfigurationProperties(RestifyConfigurationProperties.class)
@Import({RestifyDefaultConfiguration.class, RestifySpringWebConfiguration.class, RestifyAsyncConfiguration.class,
	RestifyJaxRsConfiguration.class, RestifyAutoConfigurationRegistrar.class})
@ConditionalOnMissingBean(RestifyProxyFactoryBean.class)
@AutoConfigureAfter(WebClientAutoConfiguration.class)
public class RestifyAutoConfiguration {

	protected static class RestifyAutoConfigurationRegistrar extends BaseRestifyConfigurationRegistrar {

		private RestifyableTypeScanner scanner = new RestifyableTypeScanner();

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
			doScan(AutoConfigurationPackages.get(beanFactory), scanner, registry);
		}
	}
}
