package com.restify.http.client.apache.httpclient;

import java.util.Arrays;
import java.util.Optional;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.Configurable;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

import com.restify.http.client.EndpointRequest;
import com.restify.http.client.HttpClientRequestFactory;

public class ApacheHttpClientRequestFactory implements HttpClientRequestFactory {

	private final HttpClient httpClient;
	private final String charset;

	public ApacheHttpClientRequestFactory() {
		this(HttpClients.createSystem());
	}

	public ApacheHttpClientRequestFactory(HttpClient httpClient) {
		this.httpClient = httpClient;
		this.charset = "UTF-8";
	}

	@Override
	public ApacheHttpClientRequest createOf(EndpointRequest endpointRequest) {
		HttpUriRequest httpRequest = HttpUriRequestStrategy.of(endpointRequest.method())
				.create(endpointRequest.endpoint().toString());

		HttpContext context = contextOf(httpRequest);

		endpointRequest.headers().all().forEach(h -> httpRequest.addHeader(h.name(), h.value()));

		return new ApacheHttpClientRequest(httpClient, httpRequest, context, charset);
	}

	private HttpContext contextOf(HttpUriRequest httpRequest) {
		HttpContext context = HttpClientContext.create();

		RequestConfig configuration = Optional.ofNullable(((Configurable) httpRequest).getConfig())
				.orElseGet(() -> configuration());

		context.setAttribute(HttpClientContext.REQUEST_CONFIG, configuration);

		return context;

	}

	private RequestConfig configuration() {
		return RequestConfig.custom()
				.setAuthenticationEnabled(true)
					.build();
	}

	private enum HttpUriRequestStrategy {

		GET {
			HttpUriRequest create(String endpoint) {
				return new HttpGet(endpoint);
			}
		},
		HEAD {
			HttpUriRequest create(String endpoint) {
				return new HttpHead(endpoint);
			}
		},
		POST {
			HttpUriRequest create(String endpoint) {
				return new HttpPost(endpoint);
			}
		},
		PUT {
			HttpUriRequest create(String endpoint) {
				return new HttpPut(endpoint);
			}
		},
		PATCH {
			HttpUriRequest create(String endpoint) {
				return new HttpPatch(endpoint);
			}
		},
		DELETE {
			HttpUriRequest create(String endpoint) {
				return new HttpDelete(endpoint);
			}
		},
		OPTIONS {
			HttpUriRequest create(String endpoint) {
				return new HttpOptions(endpoint);
			}
		},
		TRACE {
			HttpUriRequest create(String endpoint) {
				return new HttpTrace(endpoint);
			}
		};

		abstract HttpUriRequest create(String endpoint);

		static HttpUriRequestStrategy of(String method) {
			return Arrays.stream(HttpUriRequestStrategy.values())
					.filter(m -> m.name().equals(method))
						.findFirst()
							.orElseThrow(() -> new IllegalArgumentException("Unsupported http method: " + method));
		}
	}
}
