package it.mcsquared.engine.rest.client.oauth2;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.rest.client.servlet.APIProxyServlet;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2Proxy {
	private static final Logger logger = LoggerFactory.getLogger(Oauth2Proxy.class);

	private static final String API_AUTHORIZATION_TOKEN = "_API_AUTHORIZATION_TOKEN";

	private static String authServerBaseUrl;

	private static String redirectPath;
	private static String redirectUri;
	private static String authPath;
	// private static String authUri;
	private static String tokenPath;
	// private static String tokenUri;
	private static String refreshPath;
	// private static String refreshUri;

	private static String clientId;
	private static String clientSecret;

	public static void init(Mc2Engine engine) {
		authServerBaseUrl = engine.getSystemProperty("oauth2.auth.server.base.url");
		redirectPath = engine.getSystemProperty("oauth2.auth.server.path.redirect");
		authPath = engine.getSystemProperty("oauth2.auth.server.path.auth");
		tokenPath = engine.getSystemProperty("oauth2.auth.server.path.token");
		refreshPath = engine.getSystemProperty("oauth2.auth.server.path.refresh");

		redirectUri = authServerBaseUrl + "/" + redirectPath;
		// authUri = authServerBaseUrl + "/" + authPath;
		// tokenUri = authServerBaseUrl + "/" + tokenPath;
		// refreshUri = authServerBaseUrl + "/" + refreshPath;

		clientId = engine.getSystemProperty("oauth2.client.id");
		clientSecret = engine.getSystemProperty("oauth2.client.secret");
	}

	private Oauth2Proxy() {
	}

	public static String authorize(String clientId) throws Exception {
		logger.debug("authorizing application {}...", clientId);
		OAuthClientRequest request = OAuthClientRequest.authorizationLocation(authPath)//
				.setClientId(clientId)//
				.setRedirectURI(redirectUri)//
				.setResponseType(ResponseType.CODE.toString())//
				.setState("state").buildQueryMessage();

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		OAuthResourceResponse res = oAuthClient.resource(request, "GET", OAuthResourceResponse.class);
		String entity = res.getBody();

		String authCode = null;
		String error = null;
		JSONObject qp = new JSONObject(entity).getJSONObject("queryParameters");
		if (qp != null) {
			authCode = qp.optString(OAuth.OAUTH_CODE);
			error = qp.optString("error");
		}
		logger.debug("{} authorized, authCode: {}, error {}", clientId, authCode, error);

		if (authCode == null || authCode.isEmpty()) {
			throw new Oauth2ProxyException(error);
		}

		return authCode;
	}

	public static String requestToken(ServletContext context, String clientId, String clientSecret, String username, String password) throws Exception {
		String authCode = getAuthCode(context, clientId);

		logger.debug("requesting token [{}:{}] {}:******", clientId, authCode, username);
		OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenPath)//
				.setClientId(clientId)//
				.setClientSecret(clientSecret)//
				.setGrantType(GrantType.PASSWORD)//
				.setUsername(username)//
				.setPassword(password)//
				.setCode(authCode)//
				.setRedirectURI(redirectUri)//
				.buildBodyMessage();
		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
		String accessToken = oauthResponse.getAccessToken();
		logger.debug("{} token ok, accessToken: {}", clientId, accessToken);
		return accessToken;
	}

	public static String getAuthToken(HttpServletRequest req) {
		return (String) req.getSession().getAttribute(API_AUTHORIZATION_TOKEN);
	}

	public static void setAuthToken(HttpServletRequest req, String token) {
		req.getSession().setAttribute(API_AUTHORIZATION_TOKEN, token);
	}

	@SuppressWarnings("unchecked")
	public static String getAuthCode(ServletContext context, String clientId) throws Exception {
		Map<String, String> authCodes = (Map<String, String>) context.getAttribute(APIProxyServlet.API_AUTHORIZATION_CODES);
		String authCode = authCodes.get(clientId);
		if (authCode == null) {
			authCode = Oauth2Proxy.authorize(clientId);
			authCodes.put(clientId, authCode);
		}
		return authCode;
	}

	public static String authenticate(HttpServletRequest req, String username, String password) throws Exception {
		String authToken = getAuthToken(req);
		if (authToken == null) {
			ServletContext context = req.getSession().getServletContext();
			authToken = requestToken(context, clientId, clientSecret, username, password);
			setAuthToken(req, authToken);
		}
		return authToken;
	}
}