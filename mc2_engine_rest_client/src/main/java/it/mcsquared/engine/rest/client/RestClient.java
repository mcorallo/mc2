package it.mcsquared.engine.rest.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClient {

	private static final int TIMEOUT_MILLISEC = 10000;

	private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

	private RestClient() {
	}

	/**
	 * Executes a rest request, returning all the relevant response data.
	 * This method handles all the standard REST http methods.
	 * 
	 * All the http request generation process is delegated to the {@link RestRequest} class that already holds all the needed details.
	 * 
	 * @param request
	 * 
	 * @return A {@link RestResponse} containing an optional String serialized entity and the http response status code.
	 * 
	 * @throws Exception
	 */
	public static RestResponse executeRestRequest(RestRequest request) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpRequestBase httpRequest = request.getHttpRequest();

		HttpContext localContext = new BasicHttpContext();
		CookieStore cookieStore = new BasicCookieStore();
		for (Cookie c : request.getCookies()) {
			cookieStore.addCookie(c);
		}
		localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

		HttpResponse response = client.execute(httpRequest, localContext);
		return new RestResponse(response);
	}

}