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

package uk.co.flax.tika.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Authentication configuration.
 *
 * @author mlp
 */
public class AuthenticationConfiguration {

	public static final String DEFAULT_USERNAME_PROP = "AUTH_USERNAME";
	public static final String DEFAULT_PASSWORD_PROP = "AUTH_PASSWORD";

	@JsonProperty("enabled")
	private boolean enabled;
	@JsonProperty("usernameProperty")
	private String usernameProperty = DEFAULT_USERNAME_PROP;
	@JsonProperty("passwordProperty")
	private String passwordProperty = DEFAULT_PASSWORD_PROP;
	
	public boolean isEnabled() {
		return enabled;
	}

	public String getUsernameProperty() {
		return usernameProperty;
	}

	public String getPasswordProperty() {
		return passwordProperty;
	}

}
