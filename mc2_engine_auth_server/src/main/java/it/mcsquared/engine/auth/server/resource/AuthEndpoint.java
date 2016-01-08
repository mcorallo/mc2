package it.mcsquared.engine.auth.server.resource;

import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.auth.server.utils.TokenGenerator;
import it.mcsquared.engine.rest.server.Mc2Resource;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/auth")
public class AuthEndpoint extends Mc2Resource {

	private static final Logger logger = LoggerFactory.getLogger(AuthEndpoint.class);

	private TokenGenerator tokenGenerator;

	public AuthEndpoint() throws Exception {
		tokenGenerator = new TokenGenerator(engine);
	}

	@GET
	public Response authorize(@Context HttpServletRequest request) throws Exception {
		try {
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);

			String clientId = oauthRequest.getClientId();
			if (!Database.isValidClient(clientId)) {
				logger.debug("Invalid clientId received: {}", clientId);
				// return redirectToRegistration(oauthRequest);//the system does not allow for clients registration
				return Response.status(Status.UNAUTHORIZED).build();
			}

			OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

			// build response according to response_type
			String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

			OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);

			if (responseType.equals(ResponseType.CODE.toString())) {
				String authorizationCode = oauthIssuerImpl.authorizationCode();
				Database.addAuthCode(authorizationCode);
				builder.setCode(authorizationCode);
			} else if (responseType.equals(ResponseType.TOKEN.toString())) {
				String accessToken = tokenGenerator.generateAccessToken(oauthRequest);

				builder.setAccessToken(accessToken);
				builder.setExpiresIn(3600l);
			}

			String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
			OAuthResponse response = builder.location(redirectURI).buildQueryMessage();
			URI url = new URI(response.getLocationUri());
			return Response.status(response.getResponseStatus()).location(url).build();
		} catch (OAuthProblemException e) {
			logger.error("", e);
			Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);
			String redirectUri = e.getRedirectUri();

			if (OAuthUtils.isEmpty(redirectUri)) {
				throw new WebApplicationException(responseBuilder.entity("OAuth callback url needs to be provided by client!!!").build());
			}
			OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e).location(redirectUri).buildQueryMessage();
			URI location = new URI(response.getLocationUri());
			return responseBuilder.location(location).build();
		}
	}

	private Response redirectToRegistration(OAuthAuthzRequest oauthRequest) throws OAuthSystemException, URISyntaxException {
		ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);
		String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);

		if (OAuthUtils.isEmpty(redirectURI)) {
			throw new WebApplicationException(responseBuilder.entity("OAuth callback url needs to be provided by client!!!").build());
		}
		OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).location(redirectURI).setError("ClientId not found").buildQueryMessage();
		URI location = new URI(response.getLocationUri());
		return responseBuilder.location(location).build();
	}
}
