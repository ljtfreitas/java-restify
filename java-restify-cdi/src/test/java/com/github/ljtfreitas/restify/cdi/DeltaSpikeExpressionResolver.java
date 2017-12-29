package com.github.ljtfreitas.restify.cdi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.deltaspike.core.api.config.ConfigResolver;

import com.github.ljtfreitas.restify.http.contract.metadata.ContractExpressionResolver;

class DeltaSpikeExpressionResolver implements ContractExpressionResolver {

	private static final Pattern DYNAMIC_PATTERN = Pattern.compile("\\@\\[([a-zA-Z\\.]+)\\]");

	@Override
	public String resolve(String expression) {
		Matcher matcher = DYNAMIC_PATTERN.matcher(expression);

		if (matcher.find()) {
			StringBuffer builder = new StringBuffer();

			matcher.appendReplacement(builder, ConfigResolver.getPropertyValue(matcher.group(1), expression));

			return builder.toString();

		} else {
			return expression;
		}
	}
}
