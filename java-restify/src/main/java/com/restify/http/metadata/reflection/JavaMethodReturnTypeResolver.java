package com.restify.http.metadata.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public class JavaMethodReturnTypeResolver {

	private final Class<?> contextRawType;
	private final Type context;
	
	public JavaMethodReturnTypeResolver(Class<?> contextRawType) {
		this.contextRawType = contextRawType;
		this.context = contextRawType;
	}

	public Type resolve(Method javaMethod) {
		Type returnType = javaMethod.getGenericReturnType();
		
		return doResolve(returnType);
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
			
			return new SimpleWildcardType(new Type[]{Object.class}, newLowerBounds);
			
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

		return changed ? new SimpleParameterizedType(parameterizedType.getRawType(), newOwnerType, typeArguments) : parameterizedType;
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
}
