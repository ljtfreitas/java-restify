/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.response;

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
