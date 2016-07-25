package com.restify.http.metadata;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;

public class EndpointHeaderParameterResolver {

	private final String value;
	private final EndpointMethodParameters parameters;

	public EndpointHeaderParameterResolver(String value, EndpointMethodParameters parameters) {
		this.value = value;
		this.parameters = parameters;
	}

	public String resolve(Object[] args) {
		StringBuffer builder = new StringBuffer();

		Matcher matcher = DynamicParameterMatcher.matches(value);

		while (matcher.find()) {
			MatchResult match = matcher.toMatchResult();

			String name = match.group(1);

			parameters.find(name)
				.filter(p -> p.ofHeader())
					.ifPresent(p -> matcher.appendReplacement(builder, p.resolve(args[p.position()])));
		}

		matcher.appendTail(builder);

		return builder.toString();

	}

}
