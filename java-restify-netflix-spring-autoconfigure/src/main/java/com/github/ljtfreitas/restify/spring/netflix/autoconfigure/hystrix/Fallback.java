package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Qualifier(HystrixFallbackRegistry.QUALIFIER_NAME)
public @interface Fallback {
}
