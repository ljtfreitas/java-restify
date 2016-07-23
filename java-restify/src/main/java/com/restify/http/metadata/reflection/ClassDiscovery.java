package com.restify.http.metadata.reflection;

public class ClassDiscovery {

	public static boolean present(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
