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
package com.github.ljtfreitas.restify.http.client.message;

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

public class Range {

	private static final String BYTES_UNIT = "bytes";

	private final String unit;
	private final Long start;
	private final Long end;

	public Range(String unit, Long start, Long end) {
		this.unit = nonNull(unit, "Unit cannot be null.");
		this.start = nonNull(start, "Range start cannot be null.");
		this.end = end;
	}
	
	public String format() {
		return doFormat();
	}

	private String doFormat() {
		StringBuilder builder = new StringBuilder();

		builder.append(start)
		 	   .append("-")
			   .append(end == null ? "" : end);

		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(unit)
			   .append("=")
			   .append(doFormat());

		return builder.toString();
	}
	
	public static Range bytes(Long start) {
		return new Range(BYTES_UNIT, start, null);
	}
	
	public static Range bytes(Long start, Long end) {
		return new Range(BYTES_UNIT, start, end);
	}
	
	public static class Builder {
		
		public RangeUnit unit(String unit) {
			return new RangeUnit(unit);
		}
		
		public class RangeUnit {
			
			private final String unit;

			private RangeUnit(String unit) {
				this.unit = unit;
			}
			
			public RangeStart start(Long start) {
				return new RangeStart(unit, start);
			}

			public class RangeStart {
				
				private final String unit;
				private final Long start;

				private RangeStart(String unit, Long start) {
					this.unit = unit;
					this.start = start;
				}
				
				public RangeEnd end(Long end) {
					return new RangeEnd(unit, start, end);
				}

				public Range build() {
					return new Range(unit, start, null);
				}
				
				public class RangeEnd {
					
					private final String unit;
					private final Long start;
					private final Long end;

					private RangeEnd(String unit, Long start, Long end) {
						this.unit = unit;
						this.start = start;
						this.end = end;
					}

					public Range build() {
						return new Range(unit, start, end);
					}
				}

			}
		}
	}
}
