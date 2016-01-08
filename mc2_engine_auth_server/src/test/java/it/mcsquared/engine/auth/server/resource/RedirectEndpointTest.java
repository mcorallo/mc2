package it.mcsquared.engine.auth.server.resource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.rest.client.RestRequest.HttpMethod;
import it.mcsquared.engine.test.MockServers.RestCallHandler;
import it.mcsquared.engine.test.MockServers.RestCallStub;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class RedirectEndpointTest extends Oauth2EndpointTest {

	private static final String REDIRECT_PATH = "/oauth2/api/redirect";
	static final String REDIRECT_URI = SERVER_URL + REDIRECT_PATH;

	@Test
	public void redirectTest() throws Exception {
		String fakeClientId = "xxx3";
		String fakeClientSecret = "pwd";
		Response response = redirectRequest(fakeClientId, fakeClientSecret);
		assertEquals(200, response.getStatus());

		String authCode = getAuthCode(response);
		assertNotNull(authCode);
	}

	public static Response redirectRequest(String fakeClientId, String fakeClientSecret) throws Exception {
		Database.addClient(fakeClientId, fakeClientSecret);

		Response response = AuthEndpointTest.authRequest(fakeClientId, fakeClientSecret, REDIRECT_URI);
		assertEquals(Status.FOUND.getStatusCode(), response.getStatus());
		String redirectLocation = response.getHeaderString("location");
		assertTrue(redirectLocation.startsWith(REDIRECT_URI + "?"));

		RestCallStub redirectStub = getRedirectStub(redirectLocation);
		servers.stubCall(redirectStub);
		response = makeRequest(redirectStub, REDIRECT_URI);
		return response;
	}

	private static RestCallStub getRedirectStub(String location) {
		// http://localhost:9999/oauth2/api/redirect?state=state&code=14b745ba34d534be2d995714985adce7
		String temp = location.replace(SERVER_URL, "");
		final String[] queryString = temp.substring(temp.indexOf('?') + 1).split("&");
		RestCallStub stub = new RestCallStub(HttpMethod.POST.name(), REDIRECT_PATH);

		stub.setHandler(new RestCallHandler() {

			@Override
			public ResponseDefinitionBuilder handle(HttpServletRequest req) throws Exception {
				RedirectEndpoint redirectEndpoint = new RedirectEndpoint();
				HttpHeaders httpHeaders = mock(HttpHeaders.class);
				MultivaluedHashMap<String, String> responseHeaders = new MultivaluedHashMap<String, String>();
				responseHeaders.add("test-header", "ok");
				when(httpHeaders.getRequestHeaders()).thenReturn(responseHeaders);

				UriInfo uriInfo = mock(UriInfo.class);
				MultivaluedHashMap<String, String> responseParams = new MultivaluedHashMap<String, String>();
				for (String q : queryString) {
					String[] split = q.split("=");
					responseParams.add(split[0], split[1]);
				}
				when(uriInfo.getQueryParameters()).thenReturn(responseParams);
				String res = redirectEndpoint.redirect(httpHeaders, uriInfo);

				ResponseDefinitionBuilder response = aResponse()//
						.withStatus(200)//
						.withBody(res);
				return response;
			}
		});
		return stub;
	}

	public static String getAuthCode(Response response) throws JSONException {
		String entity = (String) response.getEntity();
		JSONObject obj = new JSONObject(entity);
		JSONObject qp = obj.getJSONObject("queryParameters");
		String authCode = null;
		if (qp != null) {
			authCode = qp.optString("code");
		}

		return authCode;
	}
}
