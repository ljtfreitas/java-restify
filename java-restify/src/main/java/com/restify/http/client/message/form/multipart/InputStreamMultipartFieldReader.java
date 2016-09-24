package com.restify.http.client.message.form.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class InputStreamMultipartFieldReader {

	private static final int BUFFER = 4096;

	private final InputStream source;

	public InputStreamMultipartFieldReader(InputStream source) {
		this.source = source;
	}

	public byte[] read() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		int read;
		byte[] data = new byte[BUFFER];

		while ((read = source.read(data, 0, data.length)) != -1) {
			output.write(data, 0, read);
		}

		return output.toByteArray();
	}

}
