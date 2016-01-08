package it.mcsquared.engine.rest.provider;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.rest.utils.TokenVerifier;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.OAuthResponse.OAuthErrorResponseBuilder;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
public class SecurityFilter implements ContainerRequestFilter {
	private static Logger logger = null;

	private static boolean enabled = true;

	@Context
	private transient HttpServletRequest request;

	private static TokenVerifier tokenVerifier;

	public static void init(Mc2Engine engine) throws Exception {
		logger = LoggerFactory.getLogger(SecurityFilter.class);
		String enabledString = engine.getSystemProperty("oauth2.security.filter.enabled");
		if (enabledString != null && !enabledString.isEmpty()) {
			enabled = Boolean.parseBoolean(enabledString);
		}

		if (enabled) {
			tokenVerifier = new TokenVerifier(engine);
		}
		logger.debug("Security filter enabled: {}" + enabled);
	}

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID);
		request.setAttribute(OAuth.OAUTH_CLIENT_ID, clientId);

		if (!enabled) {
			logger.debug("Security filter disabled");
			return;
		}

		// skip options requests
		if (context.getRequest().getMethod().equals("OPTIONS")) {
			context.abortWith(Response.status(Response.Status.OK).build());
			return;
		}

		// skip auth, token, redirect, register requests
		String path = context.getUriInfo().getPath();
		logger.info("Filtering request path: " + path);

		if (path.equals("/auth") || path.equals("/token") || path.equals("/redirect") || path.equals("/register") || path.equals("/login")) {
			logger.debug("skipped oauth2 authorization: {}", path);
			return;
		}

		// process other requests
		logger.debug("checking oauth2 authorization...");
		OAuthResponse errorResponse = null;// .getHeader(OAuth.HeaderType.WWW_AUTHENTICATE);
		String accessToken = null;
		try {
			// Make the OAuth Request out of this request
			OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.HEADER);
			accessToken = oauthRequest.getAccessToken();

			boolean valid = tokenVerifier.verify(accessToken);

			if (!valid) {
				logger.debug("invalid token");
				errorResponse = buildErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, OAuthError.ResourceResponse.INVALID_TOKEN);
			}
		} catch (OAuthProblemException e) {
			// Check if the error code has been set
			String errorCode = e.getError();
			logger.debug("invalid request ({}): {}", errorCode, e.toString());
			if (OAuthUtils.isEmpty(errorCode)) {
				errorResponse = buildErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, null);
			} else {
				errorResponse = buildErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, e.getError());// e.getDescription()
			}
		} catch (Exception e) {
			logger.error("", e);
			errorResponse = buildErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
		}

		if (errorResponse != null) {
			ResponseBuilder responseBuilder = Response.status(errorResponse.getResponseStatus());
			String header = errorResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE);
			if (header != null) {
				responseBuilder.header(OAuth.HeaderType.WWW_AUTHENTICATE, header);
			}
			context.abortWith(responseBuilder.build());
			return;
		}

		request.setAttribute(OAuth.OAUTH_ACCESS_TOKEN, accessToken);

		logger.debug("oauth2 authorization OK");
	}

	private OAuthResponse buildErrorResponse(int status, String error) {
		try {
			OAuthErrorResponseBuilder responseBuilder = OAuthResponse.errorResponse(status);
			if (error != null) {
				responseBuilder = responseBuilder.setError(error);
			}
			OAuthResponse oauthResponse = responseBuilder.buildHeaderMessage();
			return oauthResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}