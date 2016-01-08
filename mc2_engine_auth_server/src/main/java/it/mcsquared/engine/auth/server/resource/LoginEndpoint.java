package it.mcsquared.engine.auth.server.resource;

import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.rest.server.Mc2Resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.NotImplementedException;
import org.apache.oltu.oauth2.common.OAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/login")
public class LoginEndpoint extends Mc2Resource {

	private static final Logger logger = LoggerFactory.getLogger(LoginEndpoint.class);

	@Context
	private transient HttpServletRequest request;

	@POST
	public Response login(@QueryParam("username") String username, @QueryParam("password") String password) {

		String clientId = (String) request.getAttribute(OAuth.OAUTH_CLIENT_ID);

		Status status;
		try {

			boolean authenticated = Database.authenticate(clientId, username, password);

			if (authenticated) {
				status = Status.OK;
			} else {
				status = Status.UNAUTHORIZED;
			}

		} catch (Exception e) {
			logger.error("", e);
			status = Status.INTERNAL_SERVER_ERROR;
		}

		return getNoCacheResponseBuilder(status).build();
	}

	@POST
	@Path("/logout")
	public Response logout() {
		throw new NotImplementedException();
		// String accessToken = (String) request.getAttribute(OAuth.OAUTH_ACCESS_TOKEN);
		//
		// Database.logout(accessToken);
		// return getNoCacheResponseBuilder(Status.NO_CONTENT).build();
	}
}
