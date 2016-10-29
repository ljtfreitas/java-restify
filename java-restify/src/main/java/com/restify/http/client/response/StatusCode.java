package com.restify.http.client.response;

public class StatusCode {

	public static final int HTTP_STATUS_CODE_CONTINUE = 100;
	public static final int HTTP_STATUS_CODE_OK = 200;
	public static final int HTTP_STATUS_CODE_NO_CONTENT = 204;
	public static final int HTTP_STATUS_CODE_NOT_MODIFIED = 304;
	public static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
	public static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;

	private final int code;

	private StatusCode(int code) {
		this.code = code;
	}

	public int value() {
		return code;
	}

	public boolean isSucess() {
		return (code / 100) == 2;
	}

	public boolean isNotFound() {
		return code == HTTP_STATUS_CODE_NOT_FOUND;
	}

	public boolean isInformational() {
		return (code / 100) == 1;
	}

	public boolean isNoContent() {
		return code == HTTP_STATUS_CODE_NO_CONTENT;
	}

	public boolean isNotModified() {
		return code == HTTP_STATUS_CODE_NOT_MODIFIED;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StatusCode) {
			StatusCode that = (StatusCode) obj;
			return Integer.valueOf(code).equals(that.code);

		} else return false;
	}

	@Override
	public String toString() {
		return Integer.toString(code);
	}

	public static StatusCode of(int code) {
		return new StatusCode(code);
	}

	public static StatusCode contine() {
		return new StatusCode(HTTP_STATUS_CODE_CONTINUE);

	}

	public static StatusCode ok() {
		return new StatusCode(HTTP_STATUS_CODE_OK);
	}

	public static StatusCode noContent() {
		return new StatusCode(HTTP_STATUS_CODE_NO_CONTENT);
	}

	public static StatusCode notModified() {
		return new StatusCode(HTTP_STATUS_CODE_NOT_MODIFIED);
	}

	public static StatusCode notFound() {
		return new StatusCode(HTTP_STATUS_CODE_NOT_FOUND);
	}

	public static StatusCode internalServerError() {
		return new StatusCode(HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR);
	}
}
