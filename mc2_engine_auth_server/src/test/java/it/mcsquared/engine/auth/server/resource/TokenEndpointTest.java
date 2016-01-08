package it.mcsquared.engine.auth.server.resource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.rest.client.RestRequest.HttpMethod;
import it.mcsquared.engine.security.Mc2Encryption;
import it.mcsquared.engine.test.MockServers.RestCallHandler;
import it.mcsquared.engine.test.MockServers.RestCallStub;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.io.JWTReader;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TokenEndpointTest extends Oauth2EndpointTest {

	private static final String TOKEN_PATH = "/oauth2/api/token";
	private static final String TOKEN_RESOURCE = SERVER_URL + TOKEN_PATH;
	private Mc2Encryption mc2Encryption;

	@Override
	protected void localSetup() throws Exception {
		super.localSetup();

		mc2Encryption = new Mc2Encryption(((Mc2Engine) getEngine()).getConfDir());
	}

	@Test
	public void tokenTest() throws Exception {
		String fakeClientId = "xxx4";
		String fakeClientSecret = "pwd";

		Response response = RedirectEndpointTest.redirectRequest(fakeClientId, fakeClientSecret);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());

		String authCode = RedirectEndpointTest.getAuthCode(response);
		assertNotNull(authCode);

		RestCallStub tokenStub = getTokenStub(authCode, fakeClientId, fakeClientSecret);
		servers.stubCall(tokenStub);
		response = makeRequest(tokenStub, TOKEN_RESOURCE);

		assertNotNull(response);
		assertEquals(200, response.getStatus());
		String entity = (String) response.getEntity();
		JsonObject jsonObject = new Gson().fromJson(entity, JsonObject.class);

		JWTReader r = new JWTReader();
		String rawToken = jsonObject.get("access_token").getAsString();
		JWT token = r.read(rawToken);
		String signature = token.getSignature();
		String completeToken = token.getRawString();
		String tempToken = completeToken.replace(signature, "null");
		boolean valid = mc2Encryption.verifySignature(tempToken, signature);
		assertTrue(valid);

	}

	private RestCallStub getTokenStub(String authCode, String clientId, String clientSecret) {
		RestCallStub stub = new RestCallStub(HttpMethod.POST.name(), TOKEN_PATH);

		Map<String, String> params = new HashMap<String, String>();
		params.put(OAuth.OAUTH_REDIRECT_URI, RedirectEndpointTest.REDIRECT_URI);
		params.put(OAuth.OAUTH_CLIENT_ID, clientId);
		params.put(OAuth.OAUTH_CLIENT_SECRET, clientSecret);
		params.put(OAuth.OAUTH_GRANT_TYPE, GrantType.AUTHORIZATION_CODE.toString());
		params.put(OAuth.OAUTH_CODE, authCode);
		stub.setQueryParams(params);

		stub.setHandler(new RestCallHandler() {

			@Override
			public ResponseDefinitionBuilder handle(HttpServletRequest req) throws Exception {
				when(req.getContentType()).thenReturn("application/x-www-form-urlencoded");

				TokenEndpoint tokenEndpoint = new TokenEndpoint();

				Response res = tokenEndpoint.authorize(req);

				ResponseDefinitionBuilder response = aResponse()//
						.withStatus(res.getStatus())//
						.withBody((String) res.getEntity());
				return response;
			}
		});
		return stub;
	}

}
