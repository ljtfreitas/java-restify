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

<<<<<<< HEAD
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.AsyncResultEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.DeferredResultEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.ListenableFutureEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.ListenableFutureTaskEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.async.WebAsyncTaskEndpointCallExecutableAdapter;
=======
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.AsyncResultEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.DeferredResultEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.ListenableFutureEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.ListenableFutureTaskEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.async.WebAsyncTaskEndpointCallHandlerAdapter;
>>>>>>> ea4d3f4... Mudança de nomes

@Configuration
public class RestifyAsyncConfiguration {

	@Conditional(RestifyAsyncConfiguration.RestifyAsyncTaskExecutorCondition.class)
	@Bean @Async
	public AsyncListenableTaskExecutor restifyAsyncTaskExecutor() {
		return new SimpleAsyncTaskExecutor("RestifyAsyncTaskExecutor");
	}

	@Conditional(RestifyAsyncConfiguration.RestifyAsyncExecutorServiceCondition.class)
	@Bean(name = Async.EXECUTOR_SERVICE_BEAN_NAME) @Async
	public ExecutorService restifyAsyncExecutorService(@Async AsyncTaskExecutor executor) {
		return new ExecutorServiceAdapter(executor);
	}

	@ConditionalOnMissingBean
	@Bean
<<<<<<< HEAD
	public AsyncResultEndpointCallExecutableAdapter<Object, Object> asyncResultEndpointCallExecutableFactory(
			@Async AsyncTaskExecutor executor) {
		return new AsyncResultEndpointCallExecutableAdapter<>(executor);
=======
	public AsyncResultEndpointCallHandlerAdapter<Object, Object> asyncResultEndpointCallExecutableFactory(
			@Async AsyncTaskExecutor executor) {
		return new AsyncResultEndpointCallHandlerAdapter<>(executor);
>>>>>>> ea4d3f4... Mudança de nomes
	}

	@ConditionalOnMissingBean
	@Bean
<<<<<<< HEAD
	public DeferredResultEndpointCallExecutableAdapter<Object, Object> deferredResultEndpointCallExecutableFactory(
			@Async AsyncTaskExecutor executor, RestifyConfigurationProperties properties) {
		return new DeferredResultEndpointCallExecutableAdapter<>(executor, properties.getAsync().getTimeout());
=======
	public DeferredResultEndpointCallHandlerAdapter<Object, Object> deferredResultEndpointCallExecutableFactory(
			@Async AsyncTaskExecutor executor, RestifyConfigurationProperties properties) {
		return new DeferredResultEndpointCallHandlerAdapter<>(executor, properties.getAsync().getTimeout());
>>>>>>> ea4d3f4... Mudança de nomes
	}

	@ConditionalOnMissingBean
	@Bean
<<<<<<< HEAD
	public ListenableFutureEndpointCallExecutableAdapter<Object, Object> listenableFutureEndpointCallExecutableFactory(
			@Async AsyncListenableTaskExecutor executor) {
		return new ListenableFutureEndpointCallExecutableAdapter<>(executor);
=======
	public ListenableFutureEndpointCallHandlerAdapter<Object, Object> listenableFutureEndpointCallExecutableFactory(
			@Async AsyncListenableTaskExecutor executor) {
		return new ListenableFutureEndpointCallHandlerAdapter<>(executor);
>>>>>>> ea4d3f4... Mudança de nomes
	}

	@ConditionalOnMissingBean
	@Bean
<<<<<<< HEAD
	public ListenableFutureTaskEndpointCallExecutableAdapter<Object, Object> listenableFutureTaskEndpointCallExecutableFactory(
			@Async AsyncListenableTaskExecutor executor) {
		return new ListenableFutureTaskEndpointCallExecutableAdapter<>(executor);
=======
	public ListenableFutureTaskEndpointCallHandlerAdapter<Object, Object> listenableFutureTaskEndpointCallExecutableFactory(
			@Async AsyncListenableTaskExecutor executor) {
		return new ListenableFutureTaskEndpointCallHandlerAdapter<>(executor);
>>>>>>> ea4d3f4... Mudança de nomes
	}

	@ConditionalOnMissingBean
	@Bean
<<<<<<< HEAD
	public WebAsyncTaskEndpointCallExecutableAdapter<Object, Object> webAsyncTaskEndpointCallExecutableFactory(
			@Async AsyncTaskExecutor executor, RestifyConfigurationProperties properties) {
		return new WebAsyncTaskEndpointCallExecutableAdapter<>(properties.getAsync().getTimeout(), executor);
=======
	public WebAsyncTaskEndpointCallHandlerAdapter<Object, Object> webAsyncTaskEndpointCallExecutableFactory(
			@Async AsyncTaskExecutor executor, RestifyConfigurationProperties properties) {
		return new WebAsyncTaskEndpointCallHandlerAdapter<>(properties.getAsync().getTimeout(), executor);
>>>>>>> ea4d3f4... Mudança de nomes
	}

	static class RestifyAsyncTaskExecutorCondition extends AllNestedConditions {

		RestifyAsyncTaskExecutorCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnMissingBean(value = AsyncListenableTaskExecutor.class, annotation = Async.class)
		@Async
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

		@ConditionalOnMissingBean(value = ExecutorService.class, annotation = Async.class)
		@Async
		static class OnRestifyAsyncExecutorServiceMissing {
		}

		@ConditionalOnMissingBean(value = ExecutorService.class, ignored = { SchedulingTaskExecutor.class,
				TaskScheduler.class, ScheduledExecutorService.class })
		static class OnExecutorServiceMissing {
		}
	}
}