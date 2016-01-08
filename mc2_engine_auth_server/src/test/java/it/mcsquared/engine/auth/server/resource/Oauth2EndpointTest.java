package it.mcsquared.engine.auth.server.resource;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.rest.client.RestClient;
import it.mcsquared.engine.rest.client.RestRequest;
import it.mcsquared.engine.rest.client.RestRequest.HttpMethod;
import it.mcsquared.engine.rest.client.RestRequest.RestRequestBuilder;
import it.mcsquared.engine.rest.client.RestResponse;
import it.mcsquared.engine.rest.server.Mc2Resource;
import it.mcsquared.engine.security.Mc2Encryption;
import it.mcsquared.engine.test.GenericTest;
import it.mcsquared.engine.test.MockServers;
import it.mcsquared.engine.test.MockServers.RestCallStub;

import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class Oauth2EndpointTest extends GenericTest {

	private static final int SERVER_PORT = 9999;
	protected static final String SERVER_URL = "http://localhost:" + SERVER_PORT;

	protected static MockServers servers;

	private Mc2Encryption encryptionUtils;

	@BeforeClass
	public static void setupClass() throws Exception {
		servers = new MockServers();
		servers.startHttpServer(SERVER_PORT);
	}

	@AfterClass
	public static void tearDownClass() {
		servers.stopHttpServer();
	}

	@Override
	protected void localSetup() throws Exception {
		Mc2Engine engine = getEngine();
		Mc2Resource.init(engine);
		Database.init(engine);
		encryptionUtils = new Mc2Encryption(confDir);
		encryptionUtils.generateKeys();
	}

	@Override
	protected void localTearDown() throws Exception {
		encryptionUtils.deleteFiles();
	}

	protected static Response makeRequest(RestCallStub stub, String url) throws Exception {
		RestRequestBuilder builder = new RestRequestBuilder(HttpMethod.POST, url);
		Map<String, String> queryParams = stub.getQueryParams();
		if (queryParams != null) {
			for (Entry<String, String> e : queryParams.entrySet()) {
				builder.queryParameter(e.getKey(), e.getValue());
			}
		}
		Map<String, String> headers = stub.getHeaders();
		if (headers != null) {
			for (Entry<String, String> e : headers.entrySet()) {
				builder.header(e.getKey(), e.getValue());
			}
		}
		RestRequest req = builder.build();

		RestResponse res = RestClient.executeRestRequest(req);
		ResponseBuilder responseBuilder = Response.status(res.getStatusCode()).entity(res.getSerializedEntity());
		for (Entry<String, String> e : res.getHeaders().entrySet()) {
			responseBuilder.header(e.getKey(), e.getValue());
		}
		Response response = responseBuilder.build();
		return response;
	}

}
