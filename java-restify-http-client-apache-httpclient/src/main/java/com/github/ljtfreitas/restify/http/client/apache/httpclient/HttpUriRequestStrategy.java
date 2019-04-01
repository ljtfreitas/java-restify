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
package com.github.ljtfreitas.restify.http.client.apache.httpclient;

import java.util.Arrays;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;

enum HttpUriRequestStrategy {

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