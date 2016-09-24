package com.restify.http.client.response;

public class EndpointResponseCode {

	public static final int HTTP_STATUS_CODE_CONTINUE = 100;
	public static final int HTTP_STATUS_CODE_OK = 200;
	public static final int HTTP_STATUS_CODE_NO_CONTENT = 204;
	public static final int HTTP_STATUS_CODE_NOT_MODIFIED = 304;
	public static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
	public static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;

	private final int code;

	private EndpointResponseCode(int code) {
		this.code = code;
	}

	public int value() {
		return code;
	}

	public boolean isSucess() {
		return (code / 100) == 2;
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
	public String toString() {
		return Integer.toString(code);
	}

	public static EndpointResponseCode of(int code) {
		return new EndpointResponseCode(code);
	}

	public static EndpointResponseCode contine() {
		return new EndpointResponseCode(HTTP_STATUS_CODE_CONTINUE);

	}

	public static EndpointResponseCode ok() {
		return new EndpointResponseCode(HTTP_STATUS_CODE_OK);
	}

	public static EndpointResponseCode noContent() {
		return new EndpointResponseCode(HTTP_STATUS_CODE_NO_CONTENT);
	}

	public static EndpointResponseCode notModified() {
		return new EndpointResponseCode(HTTP_STATUS_CODE_NOT_MODIFIED);
	}

	public static EndpointResponseCode notFound() {
		return new EndpointResponseCode(HTTP_STATUS_CODE_NOT_FOUND);
	}

	public static EndpointResponseCode internalServerError() {
		return new EndpointResponseCode(HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR);
	}
}
