package com.restify.http.contract.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicParameterMatcher {

	private static final Pattern DYNAMIC_PARAMETER_PATTERN = Pattern.compile("\\{([a-zA-Z]+)\\}");

	public static Matcher matches(String source) {
		return DYNAMIC_PARAMETER_PATTERN.matcher(source);
	}
}
