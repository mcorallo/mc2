package it.mcsquared.engine.auth.server.resource;

import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.rest.server.Mc2Resource;
import it.mcsquared.engine.util.CryptUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.ext.dynamicreg.server.request.JSONHttpServletRequestWrapper;
import org.apache.oltu.oauth2.ext.dynamicreg.server.request.OAuthServerRegistrationRequest;
import org.apache.oltu.oauth2.ext.dynamicreg.server.response.OAuthServerRegistrationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/register")
public class RegistrationEndpoint extends Mc2Resource {
	private static final Logger logger = LoggerFactory.getLogger(RegistrationEndpoint.class);
	public static final Long TWO_YEARS = (long) (2 * 365 * 24 * 60 * 60);// seconds

	private static boolean enabled = false;

	static {
		String enabledString = engine.getSystemProperty("oauth2.registration.enabled");
		if (enabledString != null && !enabledString.isEmpty()) {
			enabled = Boolean.parseBoolean(enabledString);
		}
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response register(@Context HttpServletRequest request) throws OAuthSystemException {
		if (!enabled) {
			String error = "registration requests disabled";
			logger.error(error);
			return Response.status(Status.NOT_IMPLEMENTED).entity(error).build();
		}
		logger.debug("processing registration request...");

		try {
			JSONHttpServletRequestWrapper jsonRequest = new JSONHttpServletRequestWrapper(request);
			logger.debug("received json data: {}", jsonRequest);
			OAuthServerRegistrationRequest oauthRequest = new OAuthServerRegistrationRequest(jsonRequest, true);
			// oauthRequest.getClientUrl();
			// oauthRequest.getClientDescription();
			// oauthRequest.getRedirectURI();

			String newClientId = oauthRequest.getClientName();
			String newSecret = CryptUtils.generateRandomString(64);
			Database.addClient(newClientId, newSecret);
			OAuthResponse response = OAuthServerRegistrationResponse//
					.status(HttpServletResponse.SC_OK)//
					.setClientId(newClientId)//
					.setClientSecret(newSecret)//
					.setIssuedAt(String.valueOf(System.currentTimeMillis()))//
					.setExpiresIn(TWO_YEARS)//
					.buildJSONMessage();
			String body = response.getBody();
			logger.debug("registration ok: {}", body);
			return Response.status(response.getResponseStatus()).entity(body).build();

		} catch (OAuthProblemException e) {
			logger.error("", e);
			OAuthResponse response = OAuthServerRegistrationResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
			return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
		}

	}
}