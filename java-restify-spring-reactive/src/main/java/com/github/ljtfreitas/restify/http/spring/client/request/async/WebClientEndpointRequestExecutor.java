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
package com.github.ljtfreitas.restify.http.spring.client.request.async;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.codec.CodecException;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageException;
import com.github.ljtfreitas.restify.http.client.message.response.BaseHttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseException;
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Mono;

public class WebClientEndpointRequestExecutor implements AsyncEndpointRequestExecutor {

	private final WebClient webClient;
	private final EndpointResponseErrorFallback fallback;

	public WebClientEndpointRequestExecutor() {
		this(WebClient.create());
	}

	public WebClientEndpointRequestExecutor(WebClient webClient) {
		this(webClient, new DefaultEndpointResponseErrorFallback());
	}

	public WebClientEndpointRequestExecutor(WebClient webClient, EndpointResponseErrorFallback fallback) {
		this.webClient = webClient;
		this.fallback = fallback;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		return doExecute(endpointRequest)
			.map(e -> (EndpointResponse<T>) e)
			.block();
	}

	@Override
	public <T> CompletableFuture<EndpointResponse<T>> executeAsync(EndpointRequest endpointRequest) {
		Mono<EndpointResponse<T>> mono = doExecute(endpointRequest);
		return mono.toFuture();
	}

	private <T> Mono<EndpointResponse<T>> doExecute(EndpointRequest endpointRequest) {
		RequestBodySpec spec = webClient.method(HttpMethod.resolve(endpointRequest.method()))
			.uri(endpointRequest.endpoint())
			.headers(headers -> endpointRequest.headers().forEach(h -> headers.add(h.name(), h.value())));

		endpointRequest.body().ifPresent(body -> spec.body(fromObject(body)));

		Mono<EndpointResponse<T>> mono = spec
			.exchange()
			.flatMap(response -> read(response, endpointRequest.responseType()));

		return mono.onErrorMap(this::onError);
	}

	private <T> Mono<EndpointResponse<T>> read(ClientResponse response, JavaType responseType) {
		Optional<Mono<EndpointResponse<T>>> mono = Stream.of(response.statusCode())
				.filter(HttpStatus::isError)
				.findFirst()
				.map(status -> tryFallback(response, responseType));

		return mono.orElseGet(() -> response.bodyToMono(new JavaTypeReference<T>(responseType))
				.map(body -> convert(response, body)))
				.switchIfEmpty(Mono.defer(() -> Mono.just(convert(response, null))));
	}

	private <T> Mono<EndpointResponse<T>> tryFallback(ClientResponse response, JavaType responseType) {
		return DataBufferUtils.join(response.body(BodyExtractors.toDataBuffers()))
			.map(dataBuffer -> dataBuffer.asInputStream(true))
			.defaultIfEmpty(new ByteArrayInputStream(new byte[0]))
			.map(body -> fallback.onError(ErrorResponseMessage.create(response, body), responseType));
	}

	private <T> EndpointResponse<T> convert(ClientResponse response, T body) {
		StatusCode statusCode = StatusCode.of(response.statusCode().value());

		Headers headers = response.headers().asHttpHeaders()
				.entrySet()
				.stream()
				.reduce(new Headers(), (a, b) -> a.add(b.getKey(), b.getValue()), (a, b) -> b);

		return new EndpointResponse<>(statusCode, headers, body);
	}

	private Throwable onError(Throwable source) {
		if (source instanceof EndpointResponseException) {
			return source;

		} else if (source instanceof CodecException) {
			return new HttpMessageException(source);

		} else if (source instanceof IOException || source.getCause() instanceof IOException) {
			return new HttpClientException(source);

		} else {
			return new HttpException(source);
		}
	}

	private static class ErrorResponseMessage extends BaseHttpResponseMessage {

		private ErrorResponseMessage(StatusCode status, Headers headers, InputStream body) {
			super(status, headers, body, null);
		}

		@Override
		public void close() throws IOException {
		}

		private static ErrorResponseMessage create(ClientResponse response, InputStream body) {
			StatusCode status = StatusCode.of(response.statusCode().value(), response.statusCode().getReasonPhrase());

			Headers headers = response.headers().asHttpHeaders().entrySet().stream()
					.reduce(new Headers(), (a, b) -> a.add(b.getKey(), b.getValue()), (a, b) -> b);

			return new ErrorResponseMessage(status, headers, body);
		}
	}

	private class JavaTypeReference<T> extends ParameterizedTypeReference<T> {

		private final JavaType type;

		public JavaTypeReference(JavaType type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type.unwrap();
		}

		@Override
		public boolean equals(Object obj) {
			if ((obj instanceof JavaTypeReference)) return false;
			JavaTypeReference<?> that = (JavaTypeReference<?>) obj;
			return this == that || this.type.equals(that.type);
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public String toString() {
			return "JavaTypeReference<" + type + ">";
		}
	}
}
