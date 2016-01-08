package it.mcsquared.engine.auth.server.resource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.rest.client.RestRequest.HttpMethod;
import it.mcsquared.engine.test.MockServers.RestCallHandler;
import it.mcsquared.engine.test.MockServers.RestCallStub;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class AuthEndpointTest extends Oauth2EndpointTest {

	private static final String AUTH_PATH = "/oauth2/api/auth";
	private static final String AUTH_RESOURCE = SERVER_URL + AUTH_PATH;

	@Test
	public void validationTest() throws Exception {
		String fakeClientId = "xxx";
		RestCallStub authStub = getAuthStub(fakeClientId, null);
		servers.stubCall(authStub);

		Response response = makeRequest(authStub, AUTH_RESOURCE);
		assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
	}

	@Test
	public void authTest() throws Exception {
		String fakeClientId = "xxx2";
		String fakeClientSecret = "test-pwd";
		String redirectUri = "http://fake.server.com/test";

		Response response = authRequest(fakeClientId, fakeClientSecret, redirectUri);
		assertEquals(Status.FOUND.getStatusCode(), response.getStatus());
		assertTrue(response.getHeaderString("location").startsWith(redirectUri + "?"));

	}

	public static Response authRequest(String fakeClientId, String fakeClientSecret, String redirectUri) throws Exception {
		Database.addClient(fakeClientId, fakeClientSecret);
		RestCallStub authStub = getAuthStub(fakeClientId, redirectUri);
		servers.stubCall(authStub);

		Response response = makeRequest(authStub, AUTH_RESOURCE);
		return response;
	}

	private static RestCallStub getAuthStub(String clientId, String redirectUri) {
		RestCallStub stub = new RestCallStub(HttpMethod.POST.name(), AUTH_PATH);

		Map<String, String> params = new HashMap<String, String>();
		params.put(OAuth.OAUTH_REDIRECT_URI, redirectUri);
		params.put(OAuth.OAUTH_CLIENT_ID, clientId);
		params.put(OAuth.OAUTH_RESPONSE_TYPE, ResponseType.CODE.toString());
		params.put(OAuth.OAUTH_STATE, "state");
		stub.setQueryParams(params);

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-type", MediaType.TEXT_HTML);
		stub.setHeaders(headers);
		stub.setHandler(new RestCallHandler() {

			@Override
			public ResponseDefinitionBuilder handle(HttpServletRequest req) throws Exception {
				AuthEndpoint authEndpoint = new AuthEndpoint();
				Response res = authEndpoint.authorize(req);
				List<Object> locationHeaders = res.getMetadata().get("location");
				String redirectUrl = "";
				if (locationHeaders != null && !locationHeaders.isEmpty()) {
					redirectUrl = ((URI) locationHeaders.get(0)).toURL().toExternalForm();
				}
				return aResponse().withStatus(res.getStatus()).withHeader("location", redirectUrl);
			}
		});
		return stub;
	}

}
