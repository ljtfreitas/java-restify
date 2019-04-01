package com.github.ljtfreitas.restify.http.client.request.authentication;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.request.authentication.BasicAuthentication;
import com.github.ljtfreitas.restify.http.client.request.authentication.BasicCredentials;

@RunWith(MockitoJUnitRunner.class)
public class BasicAuthenticationTest {

	@Mock
	private BasicCredentials basicCredentials;

	@Test
	public void shouldGenerateBasicAuthenticationForUserAndPassword() {
		String content = "Basic aaa111";

		when(basicCredentials.toString()).thenReturn(content);

		BasicAuthentication basicAuthentication = new BasicAuthentication(basicCredentials);

		String authenticationContent = basicAuthentication.content(null);

		assertEquals(authenticationContent, content);
	}
}
