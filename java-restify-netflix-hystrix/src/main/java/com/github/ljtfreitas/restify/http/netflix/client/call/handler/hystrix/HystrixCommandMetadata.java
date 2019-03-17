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
package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class HystrixCommandMetadata {

	private final HystrixCommandGroupKey groupKey;
	private final HystrixCommandKey commandKey;
	private final HystrixThreadPoolKey threadPoolKey;
	private final HystrixCommandProperties.Setter commandProperties;
	private final HystrixThreadPoolProperties.Setter threadPoolProperties;

	private HystrixCommandMetadata(HystrixCommandGroupKey groupKey, HystrixCommandKey commandKey,
			HystrixThreadPoolKey threadPoolKey, HystrixCommandProperties.Setter commandProperties,
			HystrixThreadPoolProperties.Setter threadPoolProperties) {
				this.groupKey = groupKey;
				this.commandKey = commandKey;
				this.threadPoolKey = threadPoolKey;
				this.commandProperties = commandProperties;
				this.threadPoolProperties = threadPoolProperties;
	}

	public HystrixCommand.Setter asCommand() {
		return HystrixCommand.Setter
				.withGroupKey(groupKey)
					.andCommandKey(commandKey)
						.andThreadPoolKey(threadPoolKey)
							.andCommandPropertiesDefaults(commandProperties)
								.andThreadPoolPropertiesDefaults(threadPoolProperties);
	}

	public HystrixObservableCommand.Setter asObservableCommand() {
		return HystrixObservableCommand.Setter
				.withGroupKey(groupKey)
					.andCommandKey(commandKey)
						.andCommandPropertiesDefaults(commandProperties);
	}

	static class HystrixCommandMetadataBuilder {

		HystrixCommandMetadataWithGroupKeyBuilder withGroupKey(HystrixCommandGroupKey groupKey) {
			return new HystrixCommandMetadataWithGroupKeyBuilder(groupKey);
		}

		class HystrixCommandMetadataWithGroupKeyBuilder {

			private final HystrixCommandGroupKey groupKey;

			private HystrixCommandMetadataWithGroupKeyBuilder(HystrixCommandGroupKey groupKey) {
				this.groupKey = groupKey;
			}

			HystrixCommandMetadataWithCommandKeyBuilder andCommandKey(HystrixCommandKey commandKey) {
				return new HystrixCommandMetadataWithCommandKeyBuilder(this.groupKey, commandKey);
			}
		}

		class HystrixCommandMetadataWithCommandKeyBuilder {

			private final HystrixCommandGroupKey groupKey;
			private final HystrixCommandKey commandKey;

			private HystrixCommandMetadataWithCommandKeyBuilder(HystrixCommandGroupKey groupKey,
					HystrixCommandKey commandKey) {
				this.groupKey = groupKey;
				this.commandKey = commandKey;
			}

			HystrixCommandMetadataWithThreadPoolKeyBuilder andThreadPoolKey(HystrixThreadPoolKey threadPoolKey) {
				return new HystrixCommandMetadataWithThreadPoolKeyBuilder(this.groupKey, this.commandKey,
						threadPoolKey);
			}
		}

		class HystrixCommandMetadataWithThreadPoolKeyBuilder {

			private final HystrixCommandGroupKey groupKey;
			private final HystrixCommandKey commandKey;
			private final HystrixThreadPoolKey threadPoolKey;

			private HystrixCommandMetadataWithThreadPoolKeyBuilder(HystrixCommandGroupKey groupKey,
					HystrixCommandKey commandKey, HystrixThreadPoolKey threadPoolKey) {
				this.groupKey = groupKey;
				this.commandKey = commandKey;
				this.threadPoolKey = threadPoolKey;
			}

			HystrixCommandMetadataWithPropertiesBuilder andCommandPropertiesDefaults(
					HystrixCommandProperties.Setter commandProperties) {
				return new HystrixCommandMetadataWithPropertiesBuilder(this.groupKey, this.commandKey,
						this.threadPoolKey, commandProperties);
			}
		}

		class HystrixCommandMetadataWithPropertiesBuilder {

			private final HystrixCommandGroupKey groupKey;
			private final HystrixCommandKey commandKey;
			private final HystrixThreadPoolKey threadPoolKey;
			private final HystrixCommandProperties.Setter commandProperties;

			private HystrixCommandMetadataWithPropertiesBuilder(HystrixCommandGroupKey groupKey,
					HystrixCommandKey commandKey, HystrixThreadPoolKey threadPoolKey,
					HystrixCommandProperties.Setter commandProperties) {
				this.groupKey = groupKey;
				this.commandKey = commandKey;
				this.threadPoolKey = threadPoolKey;
				this.commandProperties = commandProperties;
			}

			HystrixCommandMetadataWithThreadPoolPropertiesBuilder andThreadPoolPropertiesDefaults(
					HystrixThreadPoolProperties.Setter threadPoolProperties) {
				return new HystrixCommandMetadataWithThreadPoolPropertiesBuilder(this.groupKey, this.commandKey,
						this.threadPoolKey, this.commandProperties, threadPoolProperties);
			}
		}

		class HystrixCommandMetadataWithThreadPoolPropertiesBuilder {

			private final HystrixCommandGroupKey groupKey;
			private final HystrixCommandKey commandKey;
			private final HystrixThreadPoolKey threadPoolKey;
			private final HystrixCommandProperties.Setter commandProperties;
			private final HystrixThreadPoolProperties.Setter threadPoolProperties;

			private HystrixCommandMetadataWithThreadPoolPropertiesBuilder(HystrixCommandGroupKey groupKey,
					HystrixCommandKey commandKey, HystrixThreadPoolKey threadPoolKey,
					HystrixCommandProperties.Setter commandProperties,
					HystrixThreadPoolProperties.Setter threadPoolProperties) {
				this.groupKey = groupKey;
				this.commandKey = commandKey;
				this.threadPoolKey = threadPoolKey;
				this.commandProperties = commandProperties;
				this.threadPoolProperties = threadPoolProperties;
			}

			HystrixCommandMetadata build() {
				return new HystrixCommandMetadata(this.groupKey, this.commandKey, this.threadPoolKey,
						this.commandProperties, this.threadPoolProperties);
			}
		}
	}
}
