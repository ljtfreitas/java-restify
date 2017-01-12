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
package com.github.ljtfreitas.restify.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Scope;

class RestifyProxyCdiBean implements Bean<Object> {

	private final Class<?> javaType;

	public RestifyProxyCdiBean(Class<?> javaType) {
		this.javaType = javaType;
	}

	@Override
	public Object create(CreationalContext<Object> context) {
		return new RestifyProxyCdiBeanFactory(javaType).create();
	}

	@Override
	public void destroy(Object bean, CreationalContext<Object> context) {
	}

	@Override
	public String getName() {
		return javaType.getSimpleName();
	}

	@Override
	public Set<Annotation> getQualifiers() {
		Set<Annotation> qualifiers = new LinkedHashSet<>();
		qualifiers.add(new DefaultLiteral());
		return qualifiers;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		Class<? extends Annotation> scope = Arrays.stream(javaType.getAnnotations())
			.map(a -> a.getClass())
				.filter(a -> a.getAnnotation(Scope.class) != null)
					.findFirst().orElse(null);

		return scope == null ? Dependent.class : scope;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		Set<Class<? extends Annotation>> stereotypes = new HashSet<>();

		Arrays.stream(javaType.getAnnotations())
			.map(a -> a.getClass())
				.filter(a -> a.getAnnotation(Stereotype.class) != null)
					.forEach(stereotypes::add);

		return stereotypes;
	}

	@Override
	public Set<Type> getTypes() {
		Set<Type> types = new HashSet<Type>();
		types.add(javaType);
		return types;
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public Class<?> getBeanClass() {
		return javaType;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.emptySet();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

    @SuppressWarnings("serial")
	private static final class DefaultLiteral extends AnnotationLiteral<Default> implements Default {
    }
}
