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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the User POJO.
 *
 * Created by mlp on 15/02/16.
 */
public class UserTest {

	@Test
	public void authenticated() {
		User user1 = new User(false);
		assertFalse(user1.isAuthenticated());

		User user2 = new User(true);
		assertTrue(user2.isAuthenticated());
	}

}
