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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("restify")
public class RestifyConfigurationProperties {

	private RestifyErrorProperties error = new RestifyErrorProperties();

	private RestifyAsyncProperties async = new RestifyAsyncProperties();

	public void setError(RestifyErrorProperties error) {
		this.error = error;
	}

	public RestifyErrorProperties getError() {
		return error;
	}

	public void setAsync(RestifyAsyncProperties async) {
		this.async = async;
	}

	public RestifyAsyncProperties getAsync() {
		return async;
	}

	public static class RestifyErrorProperties {

		private boolean emptyOnNotFound = false;

		public void setEmptyOnNotFound(boolean emptyOnNotFound) {
			this.emptyOnNotFound = emptyOnNotFound;
		}

		public boolean isEmptyOnNotFound() {
			return emptyOnNotFound;
		}
	}

	public static class RestifyAsyncProperties {

		private Long timeout = null;

		public void setTimeout(Long timeout) {
			this.timeout = timeout;
		}

		public Long getTimeout() {
			return timeout;
		}
	}
}
