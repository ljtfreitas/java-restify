package com.restify.http.contract.metadata.reflection;

public class JavaClassDiscovery {

	public static boolean present(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
