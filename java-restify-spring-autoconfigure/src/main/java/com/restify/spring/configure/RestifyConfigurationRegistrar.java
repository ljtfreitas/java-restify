package com.restify.spring.configure;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import com.restify.http.util.Tryable;

class RestifyConfigurationRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, ResourceLoaderAware {

	private static final Logger log = LoggerFactory.getLogger(RestifyConfigurationRegistrar.class);

	private BeanFactory beanFactory;

	private ResourceLoader resourceLoader;

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		EnableRestifyAnnotationAttributes attributes = new EnableRestifyAnnotationAttributes(
				metadata.getAnnotationAttributes(EnableRestify.class.getName(), true));

		RestifyableTypeScanner scanner = RestifyableTypeScanner.excluding(filters(attributes.exclude()));

		String[] packages = attributes.packages().length == 0 ? new String[] {packageOf(metadata.getClassName())} : attributes.packages();

		Arrays.stream(packages).forEach(p -> {
			scanner.findCandidateComponents(p)
				.stream()
					.map(candidate -> new RestifyableType(candidate.getBeanClassName()))
						.forEach(type -> create(type, registry));
		});
	}

	private void create(RestifyableType type, BeanDefinitionRegistry registry) {
		String endpoint = type.endpoint().map(e -> resolve(e)).orElse(null);

		RestifyProxyBeanBuilder builder = new RestifyProxyBeanBuilder()
				.objectType(type.objectType())
					.endpoint(endpoint);

		registry.registerBeanDefinition(type.name(), builder.build());

		log.info("Create @Restifyable bean -> {} (API [{}] metadata: Description: [{}], and endpoint: [{}])",
				type.objectType(), type.name(), type.description(), endpoint);
	}

	private String resolve(String expression) {
		return ((ConfigurableBeanFactory) beanFactory).resolveEmbeddedValue(expression);
	}

	private String packageOf(String className) {
		return Tryable.of(() -> Class.forName(className)).getPackage().getName();
	}

	@SuppressWarnings("unchecked")
	private Set<TypeFilter> filters(RestifyExcludeFilter[] filters) {
		Set<TypeFilter> typeFilters = new LinkedHashSet<>();

		Arrays.stream(filters).forEach(f -> {
			Arrays.stream(f.classes()).forEach(classType -> {
				switch (f.type()) {
					case ANNOTATION: {
						typeFilters.add(new AnnotationTypeFilter((Class<? extends Annotation>) classType));
						break;
					}
					case ASSIGNABLE_TYPE: {
						typeFilters.add(new AssignableTypeFilter(classType));
						break;
					}
					case CUSTOM: {
						typeFilters.add(Tryable.of(() -> (TypeFilter) classType.newInstance(),
								() -> new IllegalArgumentException("Cannot construct your custom TypeFilter of type [" + classType + "]")));
					}
					default: {
						throw new IllegalArgumentException("Illegal @Filter use. "
								+ "Your configure [classes] attribute with filter type [" + f.type() + "]");
					}
				}
			});

			Arrays.stream(f.pattern()).forEach(pattern -> {
				switch (f.type()) {
					case REGEX: {
						typeFilters.add(new RegexPatternTypeFilter(Pattern.compile(pattern)));
					}
					case ASPECTJ: {
						typeFilters.add(new AspectJTypeFilter(pattern, resourceLoader.getClassLoader()));
					}
					default: {
						throw new IllegalArgumentException("Illegal @Filter use. "
								+ "Your configure [pattern] attribute with filter type [" + f.type() + "]");
					}
				}
			});
		});

		return typeFilters;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	private class EnableRestifyAnnotationAttributes {

		private final Map<String, Object> attributes;

		EnableRestifyAnnotationAttributes(Map<String, Object> attributes) {
			this.attributes = attributes;
		}

		String[] packages() {
			return (String[]) attributes.get("packages");
		}

		RestifyExcludeFilter[] exclude() {
			return Arrays.stream((AnnotationAttributes[]) attributes.get("exclude"))
						.map(RestifyExcludeFilter::new)
							.toArray(RestifyExcludeFilter[]::new);
		}
	}

	private class RestifyExcludeFilter {

		private final AnnotationAttributes attributes;

		RestifyExcludeFilter(AnnotationAttributes attributes) {
			this.attributes = attributes;
		}

		Class<?>[] classes() {
			return Arrays.stream((String[]) attributes.get("classes"))
					.map(name -> Tryable.of(() -> Class.forName(name)))
						.toArray(Class<?>[]::new);
		}

		String[] pattern() {
			return (String[]) attributes.get("pattern");
		}

		FilterType type() {
			return (FilterType) attributes.get("type");
		}
	}
}