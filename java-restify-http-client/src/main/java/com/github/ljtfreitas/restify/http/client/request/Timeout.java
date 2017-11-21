package com.github.ljtfreitas.restify.http.client.request;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.ljtfreitas.restify.http.contract.metadata.Metadata;

@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Metadata
public @interface Timeout {

	long read() default -1;

	long connection() default -1;

}
