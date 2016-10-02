package com.restify.http.spring.autoconfigure;

import java.io.IOException;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

class RestifyableTypeScanner extends ClassPathScanningCandidateComponentProvider {

	RestifyableTypeScanner() {
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
}
