package it.mcsquared.engine.auth.server.resource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.rest.client.RestRequest.HttpMethod;
import it.mcsquared.engine.test.MockServers.RestCallHandler;
import it.mcsquared.engine.test.MockServers.RestCallStub;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.common.OAuth;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class LoginEndpointTest extends Oauth2EndpointTest {
	private static final String LOGIN_PATH = "/oauth2/api/login";
	static final String LOGIN_URI = SERVER_URL + LOGIN_PATH;
	static final String LOGIN_PAGE = SERVER_URL + "/oauth2/login.html";

	@Test
	public void loginTest() throws Exception {
		String fakeClientId = "xxx4";
		String fakeClientSecret = "pwd";
		String username = "un1";
		String password = "pwd1";
		Database.addUser(fakeClientId, username, password);

		// 1. auth request (redirect_uri,client_id,response_type,state)
		Response response = AuthEndpointTest.authRequest(fakeClientId, fakeClientSecret, LOGIN_PAGE);
		assertEquals(302, response.getStatus());

		// 2. redirect response -> show login form
		String redirectLocation = response.getHeaderString("location");
		// http://localhost:9999/oauth2/login.html?state=state&code=0962a7a740f52bb3bdd771da4103a8e8
		assertTrue(redirectLocation.startsWith(LOGIN_PAGE + "?"));

		// 3. the user inserts login data, data is sent along with the code query param received during the redirect
		String code = redirectLocation.substring(redirectLocation.indexOf("code=") + 5);
		RestCallStub loginStub = getLoginStub(fakeClientId, username, password, code);
		servers.stubCall(loginStub);
		response = makeRequest(loginStub, LOGIN_URI);
		assertEquals(200, response.getStatus());
	}

	public RestCallStub getLoginStub(final String clientId, final String username, final String password, String code) {
		RestCallStub stub = new RestCallStub(HttpMethod.POST.name(), LOGIN_PATH);

		Map<String, String> params = new HashMap<String, String>();
		params.put(OAuth.OAUTH_CLIENT_ID, clientId);
		params.put("username", username);
		params.put("password", password);
		stub.setQueryParams(params);

		// Map<String, String> headers = new HashMap<String, String>();
		// headers.put("Content-type", MediaType.TEXT_HTML);
		// stub.setHeaders(headers);
		stub.setHandler(new RestCallHandler() {

			@Override
			public ResponseDefinitionBuilder handle(HttpServletRequest req) throws Exception {
				LoginEndpoint loginEndpoint = new LoginEndpoint();
				req.setAttribute(OAuth.OAUTH_CLIENT_ID, clientId);
				injectMockedInstance(loginEndpoint, "request", req);

				Response res = loginEndpoint.login(username, password);
				return aResponse().withStatus(res.getStatus());
			}
		});
		return stub;
	}

}
