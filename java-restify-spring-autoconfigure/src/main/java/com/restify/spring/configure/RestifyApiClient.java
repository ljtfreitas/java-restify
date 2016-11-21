package com.restify.spring.configure;

class RestifyApiClient {

	private String endpoint;

	private RestifyApiAuthentication authentication;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public RestifyApiAuthentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(RestifyApiAuthentication authentication) {
		this.authentication = authentication;
	}

	public static class RestifyApiAuthentication {

		private Basic basic;
		private OAuth oauth;

		public Basic getBasic() {
			return basic;
		}

		public void setBasic(Basic basic) {
			this.basic = basic;
		}

		public OAuth getOauth() {
			return oauth;
		}

		public void setOauth(OAuth oauth) {
			this.oauth = oauth;
		}
	}

	public static class Basic {

		private String username;
		private String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public static class OAuth {

		private String clientId;
		private String clientSecret;

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getClientSecret() {
			return clientSecret;
		}

		public void setClientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
		}
	}
}