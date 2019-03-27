/*******************************************************************************
 *
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.ljtfreitas.restify.http.client.call.handler.guava.ListenableFutureCallbackEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.guava.ListenableFutureEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.guava.ListenableFutureTaskEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.guava.OptionalEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.jsoup.JsoupDocumentEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava.RxJavaCompletableEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava.RxJavaObservableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava.RxJavaSingleEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2CompletableEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2FlowableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2MaybeEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2ObservableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.rxjava2.RxJava2SingleEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.ArrayEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.EitherWithStringEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.EitherWithThrowableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.FutureEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.IndexedSeqEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.LazyEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.ListEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.OptionEndpointCallHandlerFactory;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.QueueEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.SeqEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.SetEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.TraversableEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.TryEndpointCallHandlerAdapter;

@Configuration
public class RestifyEndpointCallHandlersConfiguration {

	@Configuration
	@ConditionalOnClass(OptionalEndpointCallHandlerFactory.class)
	static class GuavaEndpointCallHandlersConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public OptionalEndpointCallHandlerFactory<Object> guavaOptionalEndpointCallHandlerFactory() {
			return new OptionalEndpointCallHandlerFactory<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public ListenableFutureCallbackEndpointCallHandlerAdapter<Object, Object> guavaListenableFutureCallbackEndpointCallHandlerAdapter(
				@Async ExecutorService executorService) {
			return new ListenableFutureCallbackEndpointCallHandlerAdapter<>(executorService);
		}

		@ConditionalOnMissingBean
		@Bean
		public ListenableFutureEndpointCallHandlerAdapter<Object, Object> guavaListenableFutureEndpointCallHandlerAdapter(
				@Async ExecutorService executorService) {
			return new ListenableFutureEndpointCallHandlerAdapter<>(executorService);
		}

		@ConditionalOnMissingBean
		@Bean
		public ListenableFutureTaskEndpointCallHandlerAdapter<Object, Object> guavaListenableFutureTaskEndpointCallHandlerAdapter(
				@Async ExecutorService executorService) {
			return new ListenableFutureTaskEndpointCallHandlerAdapter<>(executorService);
		}
	}

	@Configuration
	@ConditionalOnClass(JsoupDocumentEndpointCallHandlerFactory.class)
	static class JsoupEndpointCallHandlersConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public JsoupDocumentEndpointCallHandlerFactory jsoupDocumentEndpointCallHandlerFactory() {
			return new JsoupDocumentEndpointCallHandlerFactory();
		}
	}

	@Configuration
	@ConditionalOnClass(RxJava2ObservableEndpointCallHandlerAdapter.class)
	static class RxJava2EndpointCallHandlersConfiguration {

		@ConditionalOnMissingBean(value = io.reactivex.Scheduler.class, annotation = Async.class)
		@Bean @Async
		public io.reactivex.Scheduler rxJava2Scheduler() {
			return io.reactivex.schedulers.Schedulers.io();
		}

		@ConditionalOnMissingBean
		@Bean
		public RxJava2CompletableEndpointCallHandlerFactory rxJava2CompletableEndpointCallHandlerFactory(
				@Async io.reactivex.Scheduler scheduler) {
			return new RxJava2CompletableEndpointCallHandlerFactory(scheduler);
		}

		@ConditionalOnMissingBean
		@Bean
		public RxJava2FlowableEndpointCallHandlerAdapter<Object, Object> RxJava2FlowableEndpointCallHandlerAdapter(
				@Async io.reactivex.Scheduler scheduler) {
			return new RxJava2FlowableEndpointCallHandlerAdapter<>(scheduler);
		}

		@ConditionalOnMissingBean
		@Bean
		public RxJava2MaybeEndpointCallHandlerAdapter<Object, Object> rxJava2MaybeEndpointCallHandlerAdapter(
				@Async io.reactivex.Scheduler scheduler) {
			return new RxJava2MaybeEndpointCallHandlerAdapter<>(scheduler);
		}

		@ConditionalOnMissingBean
		@Bean
		public RxJava2ObservableEndpointCallHandlerAdapter<Object, Object> rxJava2ObservableEndpointCallHandlerAdapter(
				@Async io.reactivex.Scheduler scheduler) {
			return new RxJava2ObservableEndpointCallHandlerAdapter<>(scheduler);
		}

		@ConditionalOnMissingBean
		@Bean
		public RxJava2SingleEndpointCallHandlerAdapter<Object, Object> rxJava2SingleEndpointCallHandlerAdapter(
				@Async io.reactivex.Scheduler scheduler) {
			return new RxJava2SingleEndpointCallHandlerAdapter<>(scheduler);
		}
	}

	@Configuration
	@ConditionalOnClass(RxJavaObservableEndpointCallHandlerAdapter.class)
	static class RxJavaEndpointCallHandlersConfiguration {

		@ConditionalOnMissingBean(value = rx.Scheduler.class, annotation = Async.class)
		@Bean @Async
		public rx.Scheduler rxJavaScheduler() {
			return rx.schedulers.Schedulers.io();
		}

		@ConditionalOnMissingBean
		@Bean
		public RxJavaCompletableEndpointCallHandlerFactory rxJavaCompletableEndpointCallHandlerFactory(
				@Async rx.Scheduler scheduler) {
			return new RxJavaCompletableEndpointCallHandlerFactory(scheduler);
		}

		@ConditionalOnMissingBean
		@Bean
		public RxJavaObservableEndpointCallHandlerAdapter<Object, Object> rxJavaObservableEndpointCallHandlerAdapter(
				@Async rx.Scheduler scheduler) {
			return new RxJavaObservableEndpointCallHandlerAdapter<>(scheduler);
		}

		@ConditionalOnMissingBean
		@Bean
		public RxJavaSingleEndpointCallHandlerAdapter<Object, Object> rxJavaSingleEndpointCallHandlerAdapter(
				@Async rx.Scheduler scheduler) {
			return new RxJavaSingleEndpointCallHandlerAdapter<>(scheduler);
		}
	}
	
	@Configuration
	@ConditionalOnClass(OptionEndpointCallHandlerFactory.class)
	static class VavrEndpointCallHandlersConfiguration {
		
		@ConditionalOnMissingBean
		@Bean
		public ArrayEndpointCallHandlerAdapter<Object, Object> vavrArrayEndpointCallHandlerAdapter() {
			return new ArrayEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public EitherWithStringEndpointCallHandlerAdapter<Object, Object> vavrEitherWithStringEndpointCallHandlerAdapter() {
			return new EitherWithStringEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public EitherWithThrowableEndpointCallHandlerAdapter<Throwable, Object, Object> vavrEitherWithThrowableEndpointCallHandlerAdapter() {
			return new EitherWithThrowableEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public FutureEndpointCallHandlerAdapter<Object, Object> vavrFutureEndpointCallHandlerAdapter() {
			return new FutureEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public IndexedSeqEndpointCallHandlerAdapter<Object, Object> vavrIndexedSeqEndpointCallHandlerAdapter() {
			return new IndexedSeqEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public LazyEndpointCallHandlerAdapter<Object, Object> vavrLazyEndpointCallHandlerAdapter() {
			return new LazyEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public ListEndpointCallHandlerAdapter<Object, Object> vavrListEndpointCallHandlerAdapter() {
			return new ListEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public OptionEndpointCallHandlerFactory<Object> vavrOptionEndpointCallHandlerFactory() {
			return new OptionEndpointCallHandlerFactory<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public QueueEndpointCallHandlerAdapter<Object, Object> vavrQueueEndpointCallHandlerAdapter() {
			return new QueueEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public SeqEndpointCallHandlerAdapter<Object, Object> vavrSeqEndpointCallHandlerAdapter() {
			return new SeqEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public SetEndpointCallHandlerAdapter<Object, Object> vavrSetEndpointCallHandlerAdapter() {
			return new SetEndpointCallHandlerAdapter<>();
		}

		@ConditionalOnMissingBean
		@Bean
		public TraversableEndpointCallHandlerAdapter<Object, Object> traversableEndpointCallHandlerAdapter() {
			return new TraversableEndpointCallHandlerAdapter<>();
		}
		
		@ConditionalOnMissingBean
		@Bean
		public TryEndpointCallHandlerAdapter<Object, Object> vavrTryEndpointCallHandlerAdapter() {
			return new TryEndpointCallHandlerAdapter<>();
		}
	}
}