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
package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name = "oauth")
public class AccessTokenResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("access_token")
	@XmlElement(name = "access_token")
	@SerializedName("access_token")
	private String accessToken;

	@JsonProperty("token_type")
	@XmlElement(name = "token_type")
	@SerializedName("token_type")
	private String tokenType;

	@JsonProperty("expires_in")
	@XmlElement(name = "expires_in")
	@SerializedName("expires_in")
	private String expires;

	@JsonProperty("scope")
	@XmlElement(name = "scope")
	@SerializedName("scope")
	private String scope;

	@JsonProperty("refresh_token")
	@XmlElement(name = "refresh_token")
	@SerializedName("refresh_token")
	private String refreshToken;

	public String accessToken() {
		return accessToken;
	}

	public String tokenType() {
		return tokenType;
	}

	public String expires() {
		return expires;
	}

	public String scope() {
		return scope;
	}

	public String refreshToken() {
		return refreshToken;
	}

	public static AccessTokenResponse of(Parameters parameters) {
		AccessTokenResponse response = new AccessTokenResponse();

		parameters.first("access_token").ifPresent(accessToken -> response.accessToken = accessToken);

		parameters.first("token_type").ifPresent(tokenType -> response.tokenType = tokenType);

		parameters.first("expires_in").ifPresent(expires -> response.expires = expires);

		parameters.first("scope").ifPresent(scope -> response.scope = scope);

		parameters.first("refresh_token").ifPresent(refreshToken -> response.refreshToken = refreshToken);

		return response;
	}

	public static class Builder {

		private final AccessTokenResponse response = new AccessTokenResponse();

		public AccessTokenResponse.Builder token(String token) {
			response.accessToken = token;
			return this;
		}

		public AccessTokenResponse.Builder type(String tokenType) {
			response.tokenType = tokenType;
			return this;
		}

		public AccessTokenResponse.Builder expires(String expires) {
			response.expires = expires;
			return this;
		}

		public AccessTokenResponse.Builder scope(String scope) {
			response.scope = scope;
			return this;
		}

		public AccessTokenResponse.Builder refreshToken(String refreshToken) {
			response.refreshToken = refreshToken;
			return this;
		}

		public AccessTokenResponse build() {
			return response;
		}
	}
}
