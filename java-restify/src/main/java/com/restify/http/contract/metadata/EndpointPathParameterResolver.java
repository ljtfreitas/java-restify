package com.restify.http.contract.metadata;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;

public class EndpointPathParameterResolver {

	private final String path;
	private final EndpointMethodParameters parameters;

	public EndpointPathParameterResolver(String path, EndpointMethodParameters parameters) {
		this.path = path;
		this.parameters = parameters;
	}

	public String resolve(Object[] args) {
		StringBuilder endpoint = new StringBuilder();

		endpoint.append(path(args));

		return endpoint.toString();
	}

	private String path(Object[] args) {
		StringBuffer builder = new StringBuffer();

		Matcher matcher = DynamicParameterMatcher.matches(path);

		while (matcher.find()) {
			MatchResult match = matcher.toMatchResult();

			String name = match.group(1);

			parameters.find(name)
				.filter(p -> p.path())
					.ifPresent(p -> matcher.appendReplacement(builder, p.resolve(args[p.position()])));
		}

		matcher.appendTail(builder);

		return builder.toString();
	}
}
