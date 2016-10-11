package com.restify.spring.configure;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Import({RestifyConfiguration.class, RestifyConfigurationRegistrar.class})
public @interface EnableRestify {

	@AliasFor("packages")
	String[] value() default {};

	@AliasFor("value")
	String[] packages() default {};

	Filter[] exclude() default {};
}
