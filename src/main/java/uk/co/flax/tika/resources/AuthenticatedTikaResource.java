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

package uk.co.flax.tika.resources;

import io.dropwizard.auth.Auth;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import uk.co.flax.tika.api.TikaDocument;
import uk.co.flax.tika.auth.User;

/**
 * Authenticated version of the Tika class.
 *
 * @author mlp
 */
@Path("/tika")
public class AuthenticatedTikaResource extends AbstractTikaResource {
	
	@PUT @Path("/{opKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public TikaDocument handlePut(@Auth User user,
			@PathParam("opKey") String opKey, 
			@Context HttpServletRequest request,
			@Context HttpHeaders headers) {
		return handlePut(opKey, request, headers);
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String handleGet(@Auth User user) {
		return handleGet();
	}
	
}
