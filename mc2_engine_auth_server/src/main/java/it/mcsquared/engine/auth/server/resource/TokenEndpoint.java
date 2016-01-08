package it.mcsquared.engine.auth.server.resource;

import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.auth.server.utils.TokenGenerator;
import it.mcsquared.engine.rest.server.Mc2Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/token")
public class TokenEndpoint extends Mc2Resource {
	private static final Logger logger = LoggerFactory.getLogger(TokenEndpoint.class);

	public static final String INVALID_CLIENT_DESCRIPTION = "Client authentication failed.";

	private TokenGenerator tokenGenerator;

	public TokenEndpoint() throws Exception {
		tokenGenerator = new TokenGenerator(engine);
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response authorize(@Context HttpServletRequest request) throws Exception {
		logger.debug("processing token request...");
		try {
			OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
			logger.debug("received data: {}", ReflectionToStringBuilder.toString(oauthRequest));

			Response res = checkRequest(oauthRequest);
			if (res != null) {
				return res;
			}
			logger.debug("request ok");

			String accessToken = tokenGenerator.generateAccessToken(oauthRequest);
			logger.debug("generated token {}", accessToken);

			OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken(accessToken).setExpiresIn("3600").buildJSONMessage();
			return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

		} catch (OAuthProblemException e) {
			logger.error("", e);
			OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
			return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
		}
	}

	private Response checkRequest(OAuthTokenRequest oauthRequest) throws OAuthSystemException {
		logger.debug("checking token request...");
		String clientId = oauthRequest.getClientId();
		String clientSecret = oauthRequest.getClientSecret();
		String grantType = oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE);

		// check if clientid is valid
		if (!Database.isValidClient(clientId)) {
			logger.debug("invalid client {}", clientId);
			return buildErrorResponse(HttpServletResponse.SC_BAD_REQUEST, OAuthError.TokenResponse.INVALID_CLIENT);
		}

		// check if client_secret is valid
		if (!Database.authenticateClient(clientId, clientSecret)) {
			logger.debug("invalid clientId or clientSecret {} {}", clientId, clientSecret);
			return buildErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, OAuthError.TokenResponse.UNAUTHORIZED_CLIENT);
		}

		// do checking for different grant types
		if (grantType.equals(GrantType.AUTHORIZATION_CODE.toString())) {
			String authCode = oauthRequest.getParam(OAuth.OAUTH_CODE);
			if (!Database.isValidAuthCode(authCode)) {
				logger.debug("invalid authCode {}", authCode);
				return buildErrorResponse(HttpServletResponse.SC_BAD_REQUEST, OAuthError.TokenResponse.INVALID_GRANT);
			}
		} else if (grantType.equals(GrantType.PASSWORD.toString())) {
			String username = oauthRequest.getUsername();
			String password = oauthRequest.getPassword();
			if (!Database.authenticate(clientId, username, password)) {
				return buildErrorResponse(HttpServletResponse.SC_BAD_REQUEST, OAuthError.TokenResponse.INVALID_GRANT);
			}
		} else if (grantType.equals(GrantType.REFRESH_TOKEN.toString())) {
			String refreshToken = oauthRequest.getParam(OAuth.OAUTH_REFRESH_TOKEN);
			String username = oauthRequest.getUsername();
			if (!Database.isValidRefreshToken(clientId, username, refreshToken)) {
				logger.debug("invalid refreshToken {}", refreshToken);
				return buildErrorResponse(HttpServletResponse.SC_BAD_REQUEST, OAuthError.TokenResponse.INVALID_GRANT);
			}
		}
		return null;
	}

	private Response buildErrorResponse(int status, String error) throws OAuthSystemException {
		OAuthResponse response = OAuthASResponse.errorResponse(status).setError(error).setErrorDescription(INVALID_CLIENT_DESCRIPTION).buildJSONMessage();
		return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
	}
}