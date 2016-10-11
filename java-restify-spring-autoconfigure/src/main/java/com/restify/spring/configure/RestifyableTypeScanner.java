package com.restify.spring.configure;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

public class RestifyableTypeScanner extends ClassPathScanningCandidateComponentProvider {

	public RestifyableTypeScanner() {
		super(false);
		super.addIncludeFilter(new RestifyTypeFilter());
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return beanDefinition.getMetadata().isInterface();
	}

	private class RestifyTypeFilter implements TypeFilter {

		@Override
		public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
			return metadataReader.getClassMetadata().isInterface()
				&& metadataReader.getAnnotationMetadata().hasAnnotation(Restifyable.class.getName());
		}
	}

	public static RestifyableTypeScanner excluding(Set<TypeFilter> filters) {
		RestifyableTypeScanner scanner = new RestifyableTypeScanner();

		filters.forEach(f -> scanner.addExcludeFilter(f));

		return scanner;
	}
}
