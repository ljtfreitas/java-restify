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
package com.github.ljtfreitas.restify.reflection;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

public class JavaDefaultMethodExecutorTest {

	@Test
	public void shouldExecuteDefaultMethodUsingReflection() throws Throwable {
		Method method = Whatever.class.getMethod("method");

		Object output = JavaDefaultMethodExecutor.execute(method, new Whatever() {}, new Object[0]);

		assertEquals("hello", output);
	}

	@Test
	public void shouldExecuteDefaultMethodWithArgumentsUsingReflection() throws Throwable {
		Method method = Whatever.class.getMethod("methodWithArguments", String.class);

		Object output = JavaDefaultMethodExecutor.execute(method, new Whatever() {}, new Object[] {"Tiago"});

		assertEquals("hello, Tiago", output);
	}

	@Test(expected = MethodExecutionException.class)
	public void shouldThrowMethodExecutionExceptionWhenMethodCannotBeInvoked() throws Throwable {
		Method method = Private.class.getMethod("method");

		JavaDefaultMethodExecutor.execute(method, new Whatever() {}, new Object[0]);
	}
	
	public interface Whatever {
		
		default String method() {
			return "hello";
		}

		default String methodWithArguments(String name) {
			return "hello, " + name;
		}
	}
	
	private interface Private {
		
		@SuppressWarnings("unused")
		default String method() {
			return "hello";
		}
	}
}
