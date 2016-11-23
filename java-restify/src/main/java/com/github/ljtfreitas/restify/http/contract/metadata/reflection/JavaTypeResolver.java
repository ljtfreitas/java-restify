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
package com.github.ljtfreitas.restify.http.contract.metadata.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public class JavaTypeResolver {

	private final Class<?> contextRawType;
	private final Type context;

	public JavaTypeResolver(Class<?> contextRawType) {
		this.contextRawType = contextRawType;
		this.context = contextRawType;
	}

	public Type returnTypeOf(Method javaMethod) {
		Type returnType = javaMethod.getGenericReturnType();
		return doResolve(returnType);
	}

	public Type parameterizedTypeOf(Parameter parameter) {
		Type parameterizedType = parameter.getParameterizedType();
		return doResolve(parameterizedType);
	}

	private Type doResolve(Type methodReturnType) {
		Type returnType = methodReturnType;

		while (true) {
			if (returnType instanceof TypeVariable) {
				TypeVariable<?> typeVariable = (TypeVariable<?>) returnType;
				returnType = doResolveTypeVariable(typeVariable);
				if (returnType == typeVariable) return returnType;

			} else if (returnType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) returnType;
				return doResolveParameterizedType(parameterizedType);

			} else if (returnType instanceof Class && ((Class<?>) returnType).isArray()) {
				Class<?> arrayClassType = (Class<?>) returnType;
				return doResolveArrayClassType(arrayClassType);

			} else if (returnType instanceof GenericArrayType) {
				GenericArrayType genericArrayType = (GenericArrayType) returnType;
				return doResolveGenericArrayType(genericArrayType);

			} else if (returnType instanceof WildcardType) {
				WildcardType wildcardType = (WildcardType) returnType;
				return doResolveWildcardType(wildcardType);

			} else {
				return returnType;
			}
		}
	}

	private Type doResolveWildcardType(WildcardType wildcardType) {
		Type[] lowerBounds = wildcardType.getLowerBounds();
		Type[] upperBounds = wildcardType.getUpperBounds();

		if (lowerBounds.length != 0) {
			Type[] newLowerBounds = new Type[lowerBounds.length];

			for (int position = 0; position < lowerBounds.length; position++) {
				newLowerBounds[position] = doResolve(lowerBounds[position]);
			}

			return new SimpleWildcardType(new Type[] { Object.class }, newLowerBounds);

		} else if (upperBounds.length != 0) {
			Type[] newUpperBounds = new Type[upperBounds.length];

			for (int position = 0; position < upperBounds.length; position++) {
				newUpperBounds[position] = doResolve(upperBounds[position]);
			}

			return new SimpleWildcardType(newUpperBounds, new Type[0]);
		}

		return wildcardType;
	}

	private Type doResolveGenericArrayType(GenericArrayType genericArrayType) {
		Type componentType = genericArrayType.getGenericComponentType();
		Type newComponentType = doResolve(componentType);
		return componentType == newComponentType ? genericArrayType : new SimpleGenericArrayType(newComponentType);
	}

	private Type doResolveArrayClassType(Class<?> arrayClassType) {
		Type componentType = arrayClassType.getComponentType();
		Type newComponentType = doResolve(componentType);
		return componentType == newComponentType ? arrayClassType : new SimpleGenericArrayType(newComponentType);
	}

	private Type doResolveParameterizedType(ParameterizedType parameterizedType) {
		Type ownerType = parameterizedType.getOwnerType();
		Type newOwnerType = doResolve(ownerType);

		boolean changed = (ownerType != newOwnerType);

		Type[] typeArguments = parameterizedType.getActualTypeArguments();

		for (int position = 0; position < typeArguments.length; position++) {
			Type resolvedTypeArgument = doResolve(typeArguments[position]);

			if (resolvedTypeArgument != typeArguments[position]) {
				if (!changed) {
					typeArguments = typeArguments.clone();
					changed = true;
				}
				typeArguments[position] = resolvedTypeArgument;
			}
		}

		return changed ? new SimpleParameterizedType(parameterizedType.getRawType(), newOwnerType, typeArguments)
				: parameterizedType;
	}

	private Type doResolveTypeVariable(TypeVariable<?> typeVariable) {
		GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();

		if (genericDeclaration instanceof Class) {
			Class<?> declaredClassType = (Class<?>) genericDeclaration;
			Type declaredType = doResolveGenericSuperType(context, contextRawType, declaredClassType);

			if (declaredType instanceof ParameterizedType) {
				return ((ParameterizedType) declaredType)
						.getActualTypeArguments()[Arrays.asList(declaredClassType.getTypeParameters()).indexOf(typeVariable)];
			}
		}

		return typeVariable;
	}

	private Type doResolveGenericSuperType(Type context, Class<?> contextRawType, Class<?> classType) {
		if (context == classType) {
			return context;
		}

		if (classType.isInterface()) {
			for (int position = 0; position < contextRawType.getInterfaces().length; position++) {
				Class<?> interfaceType = contextRawType.getInterfaces()[position];

				if (interfaceType == classType) {
					return contextRawType.getGenericInterfaces()[position];

				} else if (classType.isAssignableFrom(contextRawType.getInterfaces()[position])) {
					return doResolveGenericSuperType(contextRawType.getGenericInterfaces()[position], interfaceType, classType);
				}
			}
		}

		return classType;
	}

	public static Class<?> rawClassTypeOf(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;

		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			return rawClassTypeOf(parameterizedType.getRawType());

		} else if (type instanceof GenericArrayType) {
			GenericArrayType genericArrayType = (GenericArrayType) type;
			return Array.newInstance(rawClassTypeOf(genericArrayType.getGenericComponentType()), 0).getClass();

		} else if (type instanceof WildcardType) {
			WildcardType wildcardType = (WildcardType) type;
			return rawClassTypeOf(wildcardType.getUpperBounds()[0]);

		} else if (type instanceof TypeVariable) {
			return Object.class;

		} else {
			throw new IllegalArgumentException("The raw Class of type [" + type + "] cannot be determined");

		}
	}
}
