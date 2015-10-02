/**
 * Copyright (c) 2015 Lemur Consulting Ltd.
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

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import org.apache.commons.lang3.StringUtils;

import uk.co.flax.tika.config.AuthenticationConfiguration;

import com.google.common.base.Optional;

/**
 * Basic authentication.
 *
 * @author mlp
 */
public class BasicAuthenticator implements Authenticator<BasicCredentials, User> {
	
	private final AuthenticationConfiguration config;
	
	private final String username;
	private final String password;
	
	public BasicAuthenticator(AuthenticationConfiguration config) {
		this.config = config;
		this.username = System.getProperty(config.getUsernameProperty());
		this.password = System.getProperty(config.getPasswordProperty());
	}

	@Override
	public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
		Optional<User> option;
		
		if (!config.isEnabled()) {
			option = Optional.of(new User(true));
		} else {
			if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
				throw new AuthenticationException("No username or password to check against!");
			}
			
			if (credentials.getUsername().equals(username) && credentials.getPassword().equals(password)) {
				option = Optional.of(new User(true));
			} else {
				option = Optional.absent();
			}
		}
		
		return option;
	}
	
	
}
