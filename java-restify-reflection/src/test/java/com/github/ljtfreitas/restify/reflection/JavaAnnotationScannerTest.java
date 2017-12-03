package com.github.ljtfreitas.restify.reflection;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class JavaAnnotationScannerTest {

	private JavaAnnotationScanner scanner;

	@Before
	public void setup() {
		scanner = new JavaAnnotationScanner(MyType.class);
	}

	@Test
	public void shouldFindMetaAnnotationMarkedWithEmbeddedAnnotation() {
		Optional<Annotation> annotation = scanner.with(Embedded.class);

		assertTrue(annotation.isPresent());
		assertThat(annotation.get(), instanceOf(Meta.class));
	}

	@Test
	public void shouldFindAllMetaAnnotationsMarkedWithEmbeddedAnnotation() {
		Collection<Annotation> annotations = scanner.allWith(Embedded.class);

		assertThat(annotations, not(empty()));
		assertThat(annotations, contains(instanceOf(Meta.class)));
	}

	@Test
	public void shouldScanAnnotationByType() {
		Optional<Meta> annotation = scanner.scan(Meta.class);

		assertTrue(annotation.isPresent());
	}

	@Test
	public void shouldScanAllAnnotationByType() throws Exception {
		scanner = new JavaAnnotationScanner(MyType.class.getMethod("method"));

		Collection<Meta> annotations = scanner.scanAll(Meta.class);

		assertThat(annotations, not(empty()));
	}

	@Test
	public void containsShouldReturnTrueWhenElementHasAnnotation() {
		assertTrue(scanner.contains(Meta.class));
	}

	@Test
	public void containsShouldReturnFalseWhenElementHasAnnotation() {
		assertFalse(scanner.contains(Other.class));
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.ANNOTATION_TYPE)
	@Inherited
	@interface Embedded {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE,ElementType.METHOD})
	@interface Container {
		
		Meta[] value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE,ElementType.METHOD})
	@Embedded
	@Repeatable(Container.class)
	@interface Meta {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Other {
	}
	
	@Meta
	interface MyType {

		@Meta
		@Meta
		void method();
	}
}
