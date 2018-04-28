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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.TaskScheduler;

import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.AsyncResultEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.DeferredResultEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.ListenableFutureEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.ListenableFutureTaskEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.WebAsyncTaskEndpointCallExecutableFactory;

@Configuration
public class RestifyAsyncConfiguration {

	@Conditional(RestifyAsyncConfiguration.RestifyAsyncTaskExecutorCondition.class)
	@Bean
	public AsyncListenableTaskExecutor restifyAsyncTaskExecutor() {
		return new SimpleAsyncTaskExecutor("RestifyAsyncTaskExecutor");
	}

	@Conditional(RestifyAsyncConfiguration.RestifyAsyncExecutorServiceCondition.class)
	@Bean
	public ExecutorService restifyAsyncExecutorService(@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor) {
		return new ExecutorServiceAdapter(executor);
	}

	@ConditionalOnMissingBean
	@Bean
	public AsyncResultEndpointCallExecutableFactory<Object, Object> asyncResultEndpointCallExecutableFactory(
			@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor) {
		return new AsyncResultEndpointCallExecutableFactory<>(executor);
	}

	@ConditionalOnMissingBean
	@Bean
	public DeferredResultEndpointCallExecutableFactory<Object, Object> deferredResultEndpointCallExecutableFactory(
			@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor, RestifyConfigurationProperties properties) {
		return new DeferredResultEndpointCallExecutableFactory<>(executor, properties.getAsync().getTimeout());
	}

	@ConditionalOnMissingBean
	@Bean
	public ListenableFutureEndpointCallExecutableFactory<Object, Object> listenableFutureEndpointCallExecutableFactory(
			@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
		return new ListenableFutureEndpointCallExecutableFactory<>(executor);
	}

	@ConditionalOnMissingBean
	@Bean
	public ListenableFutureTaskEndpointCallExecutableFactory<Object, Object> listenableFutureTaskEndpointCallExecutableFactory(
			@Qualifier("restifyAsyncTaskExecutor") AsyncListenableTaskExecutor executor) {
		return new ListenableFutureTaskEndpointCallExecutableFactory<>(executor);
	}

	@ConditionalOnMissingBean
	@Bean
	public WebAsyncTaskEndpointCallExecutableFactory<Object, Object> webAsyncTaskEndpointCallExecutableFactory(
			@Qualifier("restifyAsyncTaskExecutor") AsyncTaskExecutor executor, RestifyConfigurationProperties properties) {
		return new WebAsyncTaskEndpointCallExecutableFactory<>(properties.getAsync().getTimeout(), executor);
	}

	static class RestifyAsyncTaskExecutorCondition extends AllNestedConditions {

		RestifyAsyncTaskExecutorCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnMissingBean(name = "restifyAsyncTaskExecutor")
		static class OnRestifyAsyncTaskExecutorMissing {
		}

		@ConditionalOnMissingBean(value = AsyncListenableTaskExecutor.class, ignored = {
				SchedulingTaskExecutor.class, TaskScheduler.class, ScheduledExecutorService.class })
		static class OnAsyncListenableTaskExecutorMissing {
		}
	}

	static class RestifyAsyncExecutorServiceCondition extends AllNestedConditions {

		RestifyAsyncExecutorServiceCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnMissingBean(name = "restifyAsyncExecutorService")
		static class OnRestifyAsyncExecutorServiceMissing {
		}

		@ConditionalOnMissingBean(value = ExecutorService.class, ignored = { SchedulingTaskExecutor.class,
				TaskScheduler.class, ScheduledExecutorService.class })
		static class OnExecutorServiceMissing {
		}
	}
}