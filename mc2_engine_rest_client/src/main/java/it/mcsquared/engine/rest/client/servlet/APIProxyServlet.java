package it.mcsquared.engine.rest.client.servlet;

import it.mcsquared.engine.rest.client.RestClient;
import it.mcsquared.engine.rest.client.RestRequest;
import it.mcsquared.engine.rest.client.RestRequest.HttpMethod;
import it.mcsquared.engine.rest.client.RestRequest.RestRequestBuilder;
import it.mcsquared.engine.rest.client.RestResponse;
import it.mcsquared.engine.rest.client.oauth2.Oauth2Proxy;
import it.mcsquared.engine.rest.client.oauth2.Oauth2ProxyException;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@WebServlet("/api/*")
public class APIProxyServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(APIProxyServlet.class);

	public static final String API_AUTHORIZATION_CODES = "_API_AUTHORIZATION_CODES";

	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		initOauth2Proxy(context);
	}

	@SuppressWarnings("unchecked")
	public static void initOauth2Proxy(ServletContext context) {
		// FIXME [mc2] gestire piu' modi di autenticazione per diversi servizi

		Map<String, String> authCodes = (Map<String, String>) context.getAttribute(API_AUTHORIZATION_CODES);
		if (authCodes == null) {
			authCodes = new ConcurrentHashMap<String, String>();
			context.setAttribute(API_AUTHORIZATION_CODES, authCodes);
		}
		logger.debug("Oauth2Proxy initialized");
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("received oauth2 request, checking authorization...");
		try {
			String token = Oauth2Proxy.getAuthToken(req);
			if (token == null) {
				logger.debug("authorization failed, token null");
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			logger.debug("authorization ok, token {}", token);

			makeRestServiceCall(req, resp, token);
		} catch (Oauth2ProxyException e) {
			throw e;
		} catch (Exception e) {
			throw new Oauth2ProxyException(e);
		}
	}

	private void makeRestServiceCall(HttpServletRequest req, HttpServletResponse resp, String token) throws IOException, Exception {
		HttpMethod httpMethod = HttpMethod.valueOf(req.getMethod());
		RestRequestBuilder builder = new RestRequestBuilder(httpMethod, "http://localhost:8080/mc2_test_rest_service/rest");// FIXME [oauth2] spostare in properties
		String pathInfo = req.getPathInfo();
		for (String p : pathInfo.split("/")) {
			if (p != null && !p.isEmpty()) {
				builder.pathSegment(p);
			}
		}
		Enumeration<String> parameters = req.getParameterNames();
		while (parameters.hasMoreElements()) {
			String p = (String) parameters.nextElement();
			builder.queryParameter(p, req.getParameter(p));
		}

		Enumeration<String> headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String h = (String) headerNames.nextElement();
			builder.header(h, req.getHeader(h));
		}
		builder.header("authorization", "Bearer " + token);

		if (httpMethod != HttpMethod.GET) {
			String reqBody = IOUtils.toString(req.getInputStream());
			builder.body(reqBody);
		}
		RestRequest rr = builder.build();
		logger.debug("making rest request to: {}", rr);
		RestResponse response = RestClient.executeRestRequest(rr);
		// FIXME [mc2] verificare se il token e' scaduto: refresh
		resp.setStatus(response.getStatusCode());
		resp.getWriter().write(response.getSerializedEntity());
	}

	class RequestWrapper extends HttpServletRequestWrapper {

		private String servletPath;
		private String contextPath;
		private Map<String, String> headerMap = new HashMap<>();

		public RequestWrapper(HttpServletRequest request) {
			super(request);
		}

		void addHeader(String name, String value) {
			headerMap.put(name, value);
		}

		@Override
		public String getHeader(String name) {
			String headerValue = super.getHeader(name);
			if (headerMap.containsKey(name)) {
				headerValue = headerMap.get(name);
			}
			return headerValue;
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			List<String> names = Collections.list(super.getHeaderNames());
			for (String name : headerMap.keySet()) {
				names.add(name);
			}
			return Collections.enumeration(names);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			List<String> values = Collections.list(super.getHeaders(name));
			if (headerMap.containsKey(name)) {
				values.add(headerMap.get(name));
			}
			return Collections.enumeration(values);
		}

		@Override
		public String getServletPath() {
			return this.servletPath != null ? this.servletPath : super.getServletPath();
		}

		public void setServletPath(String servletPath) {
			this.servletPath = servletPath;
		}

		@Override
		public String getContextPath() {
			return this.contextPath != null ? this.contextPath : super.getContextPath();
		}

		public void setContextPath(String contextPath) {
			this.contextPath = contextPath;
		}
	}
}