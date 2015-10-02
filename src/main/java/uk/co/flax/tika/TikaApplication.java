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

package uk.co.flax.tika;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.setup.Environment;
import uk.co.flax.tika.auth.BasicAuthenticator;
import uk.co.flax.tika.auth.User;
import uk.co.flax.tika.healthchecks.PingHealthcheck;
import uk.co.flax.tika.resources.AuthenticatedTikaResource;
import uk.co.flax.tika.resources.TikaResource;

/**
 * Main class for the Tika server application.
 *
 * @author mlp
 */
public class TikaApplication extends Application<TikaConfiguration> {

	@Override
	public void run(TikaConfiguration config, Environment env) throws Exception {
		if (config.getAuthentication().isEnabled()) {
			// Add authenticator
			env.jersey().register(AuthFactory.binder(
					new BasicAuthFactory<User>(
							new BasicAuthenticator(config.getAuthentication()), 
							"Protected Tika server", 
							User.class)));

			// Register the authenticated Tika resource
			env.jersey().register(new AuthenticatedTikaResource());
		} else {
			// Register the non-authenticated Tika resource
			env.jersey().register(new TikaResource());
		}
		
		env.healthChecks().register("Ping", new PingHealthcheck());
	}
	
	public static void main(String... args) throws Exception {
		new TikaApplication().run(args);
	}

}
