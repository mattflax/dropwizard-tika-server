/**
 * Copyright (c) 2016 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.flax.tika.auth;

import com.google.common.base.Optional;
import io.dropwizard.auth.basic.BasicCredentials;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.flax.tika.config.AuthenticationConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the BasicAuthenticator.
 *
 * Created by mlp on 15/02/16.
 */
public class BasicAuthenticatorTest {

	private static String systemUsername;
	private static String systemPassword;

	private AuthenticationConfiguration authConfig;

	@BeforeClass
	public static void readSystemProperties() {
		systemUsername = System.getProperty(AuthenticationConfiguration.DEFAULT_USERNAME_PROP);
		systemPassword = System.getProperty(AuthenticationConfiguration.DEFAULT_PASSWORD_PROP);
	}

	@AfterClass
	public static void cleanupSystemProperties() {
		if (systemUsername != null) {
			System.setProperty(AuthenticationConfiguration.DEFAULT_USERNAME_PROP, systemUsername);
		}
		if (systemPassword != null) {
			System.setProperty(AuthenticationConfiguration.DEFAULT_PASSWORD_PROP, systemPassword);
		}
	}

	@Before
	public void initialiseConfiguration() {
		authConfig = mock(AuthenticationConfiguration.class);
		when(authConfig.getUsernameProperty()).thenReturn(AuthenticationConfiguration.DEFAULT_USERNAME_PROP);
		when(authConfig.getPasswordProperty()).thenReturn(AuthenticationConfiguration.DEFAULT_PASSWORD_PROP);
	}


	@Test
	public void authenticate_authenticationDisabled() throws Exception {
		when(authConfig.isEnabled()).thenReturn(false);

		BasicCredentials credentials = mock(BasicCredentials.class);

		BasicAuthenticator auth = new BasicAuthenticator(authConfig);
		Optional<User> user = auth.authenticate(credentials);

		assertTrue(user.isPresent());
		assertTrue(user.get().isAuthenticated());

		verify(authConfig).isEnabled();
	}

	@Test(expected=io.dropwizard.auth.AuthenticationException.class)
	public void authenticate_noUsernameInConfig() throws Exception {
		when(authConfig.isEnabled()).thenReturn(true);

		System.setProperty(AuthenticationConfiguration.DEFAULT_USERNAME_PROP, "");

		BasicCredentials credentials = mock(BasicCredentials.class);

		BasicAuthenticator auth = new BasicAuthenticator(authConfig);
		auth.authenticate(credentials);

		verify(authConfig).isEnabled();
	}

	@Test(expected=io.dropwizard.auth.AuthenticationException.class)
	public void authenticate_noPasswordInConfig() throws Exception {
		when(authConfig.isEnabled()).thenReturn(true);

		System.setProperty(AuthenticationConfiguration.DEFAULT_USERNAME_PROP, "fred");
		System.setProperty(AuthenticationConfiguration.DEFAULT_PASSWORD_PROP, "");

		BasicCredentials credentials = mock(BasicCredentials.class);

		BasicAuthenticator auth = new BasicAuthenticator(authConfig);
		auth.authenticate(credentials);

		verify(authConfig).isEnabled();
	}

	@Test
	public void authenticate_noMatchForUsername() throws Exception {
		when(authConfig.isEnabled()).thenReturn(true);

		System.setProperty(AuthenticationConfiguration.DEFAULT_USERNAME_PROP, "fred");
		System.setProperty(AuthenticationConfiguration.DEFAULT_PASSWORD_PROP, "pass");

		BasicCredentials credentials = mock(BasicCredentials.class);
		when(credentials.getUsername()).thenReturn("john");

		BasicAuthenticator auth = new BasicAuthenticator(authConfig);
		Optional<User> user = auth.authenticate(credentials);

		assertFalse(user.isPresent());

		verify(authConfig).isEnabled();
		verify(credentials).getUsername();
	}

	@Test
	public void authenticate_noMatchForPassword() throws Exception {
		when(authConfig.isEnabled()).thenReturn(true);

		System.setProperty(AuthenticationConfiguration.DEFAULT_USERNAME_PROP, "fred");
		System.setProperty(AuthenticationConfiguration.DEFAULT_PASSWORD_PROP, "pass");

		BasicCredentials credentials = mock(BasicCredentials.class);
		when(credentials.getUsername()).thenReturn("fred");
		when(credentials.getPassword()).thenReturn("fred's password");

		BasicAuthenticator auth = new BasicAuthenticator(authConfig);
		Optional<User> user = auth.authenticate(credentials);

		assertFalse(user.isPresent());

		verify(authConfig).isEnabled();
		verify(credentials).getUsername();
		verify(credentials).getPassword();
	}

	@Test
	public void authenticate_successful() throws Exception {
		when(authConfig.isEnabled()).thenReturn(true);

		System.setProperty(AuthenticationConfiguration.DEFAULT_USERNAME_PROP, "fred");
		System.setProperty(AuthenticationConfiguration.DEFAULT_PASSWORD_PROP, "pass");

		BasicCredentials credentials = mock(BasicCredentials.class);
		when(credentials.getUsername()).thenReturn("fred");
		when(credentials.getPassword()).thenReturn("pass");

		BasicAuthenticator auth = new BasicAuthenticator(authConfig);
		Optional<User> user = auth.authenticate(credentials);

		assertTrue(user.isPresent());
		assertTrue(user.get().isAuthenticated());

		verify(authConfig).isEnabled();
		verify(credentials).getUsername();
		verify(credentials).getPassword();
	}

}
