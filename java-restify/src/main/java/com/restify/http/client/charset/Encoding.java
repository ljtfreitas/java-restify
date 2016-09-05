package com.restify.http.client.charset;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public interface Encoding {

	public static final Encoding UTF_8 = new Encoding() {
		@Override
		public Charset charset() {
			return Charset.forName("UTF-8");
		}
	};

	public static final Encoding ISO_8859_1 = new Encoding() {
		@Override
		public Charset charset() {
			return Charset.forName("ISO-8859-1");
		}
	};

	public Charset charset();

	public default String encode(String value) {
		try {
			return URLEncoder.encode(value, charset().name());
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}

}
