package it.mcsquared.engine.test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.SmtpServer;
import com.dumbster.smtp.SmtpServerFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

public class MockServers {

	public static interface EmailFields {

		public static final String SUBJECT = "SUBJECT";
		public static final String TO = "TO";
		public static final String CC = "CC";
		public static final String BODY = "BODY";
	}

	private static Logger logger = LoggerFactory.getLogger(MockedTestHelper.class);
	private static String contextPath = "mc2-test";

	private WireMockServer wireMockServer = null;
	private SmtpServer mailServer = null;
	private Map<String, Object> sessionDataMap;

	public void startHttpServer(int port) {

		try {

			logger.debug("Starting mock HTTP server at port {}", port);

			if (null != wireMockServer && wireMockServer.isRunning()) {

				throw new IllegalStateException("Mock HTTP server already started at port " + port);
			}

			wireMockServer = new WireMockServer(port);
			WireMock.configureFor("localhost", port);
			wireMockServer.start();
			logger.debug("Mock HTTP server started at port {}", port);
		} catch (Exception e) {

			logger.error("Unable to start mock HTTP server at port {}", port, e);
			throw e;
		}
	}

	public void stopHttpServer() {

		int port = wireMockServer.port();

		try {

			WireMock.reset();
			wireMockServer.stop();
			wireMockServer = null;
			logger.debug("Mock HTTP server stopped, port: {}", port);
		} catch (Exception e) {

			logger.error("Unable to stop mock HTTP server, port: {}", port, e);
			throw e;
		}
	}

	public void startMailServer(int port) {

		try {

			logger.debug("Starting Mock SMTP server at port {}", port);

			if (null != mailServer && !mailServer.isStopped()) {

				throw new IllegalStateException("Mock SMTP server already started at port " + port);
			}

			mailServer = SmtpServerFactory.startServer();
			logger.debug("Mock SMTP server started at port {}", port);
		} catch (Exception e) {

			logger.error("Unable to start mock SMTP server at port {}", port, e);
			throw e;
		}
	}

	public void stopMailServer() {

		try {

			mailServer.stop();
			logger.debug("Mock SMTP server stopped");
			mailServer = null;
		} catch (Exception e) {

			logger.error("Unable to stop mock SMTP server", e);
			throw e;
		}
	}

	public int getReceivedEmailCount() {

		return mailServer.getEmailCount();
	}

	public Map<String, String> getLastEmailData() {

		Map<String, String> emailData = new HashMap<String, String>();
		MailMessage email = mailServer.getMessages()[0];
		emailData.put(EmailFields.SUBJECT, email.getFirstHeaderValue("Subject"));
		emailData.put(EmailFields.TO, email.getFirstHeaderValue("To"));
		emailData.put(EmailFields.CC, email.getFirstHeaderValue("Cc"));
		emailData.put(EmailFields.BODY, email.getBody());

		return emailData;
	}

	public Map<String, String> getEmailDataWithSubject(String subject) {

		Map<String, String> emailData = new HashMap<String, String>();
		for (MailMessage email : mailServer.getMessages()) {
			String s = email.getFirstHeaderValue("Subject");
			if (s != null && s.equals(subject)) {
				emailData.put(EmailFields.SUBJECT, email.getFirstHeaderValue("Subject"));
				emailData.put(EmailFields.TO, email.getFirstHeaderValue("To"));
				emailData.put(EmailFields.CC, email.getFirstHeaderValue("Cc"));
				emailData.put(EmailFields.BODY, email.getBody());

				return emailData;
			}
		}

		return null;
	}

	public void removeAllEmails() {

		mailServer.clearMessages();
	}

	public void stubRestFileDownload(String restRequestUrl, File testFile, String attachmentFileName) throws IOException {

		ResponseDefinitionBuilder response = aResponse()//
				.withStatus(200)//
				.withHeader("Content-Type", "application/octet-stream")//
				.withHeader("Content-Disposition", "attachment;filename=\"" + attachmentFileName + "\"")//
				.withBody(testFile != null ? FileUtils.readFileToByteArray(testFile) : null);

		stubFor(get(urlMatching(restRequestUrl)).willReturn(response));
	}

	public void stubRestJsonData(String restRequestUrl, String jsonData, String httpMethod) throws IOException {

		ResponseDefinitionBuilder response = aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(jsonData);

		MappingBuilder mappingBuilder;
		switch (httpMethod.toLowerCase()) {
		case "get":
			mappingBuilder = get(urlMatching(restRequestUrl));
			break;
		case "post":
			mappingBuilder = post(urlMatching(restRequestUrl));
			break;

		default:
			mappingBuilder = get(urlMatching(restRequestUrl));
			break;
		}
		stubFor(mappingBuilder.willReturn(response));
	}

	public void stubCall(RestCallStub stub) throws Exception {
		MappingBuilder mappingBuilder;
		String restRequestUrl = stub.getUrl() + "(.)*";
		switch (stub.getHttpMethod().toLowerCase()) {
		case "get":
			mappingBuilder = get(urlMatching(restRequestUrl));
			break;
		case "post":
			mappingBuilder = post(urlMatching(restRequestUrl));
			break;

		default:
			mappingBuilder = get(urlMatching(restRequestUrl));
			break;
		}

		Map<String, String> queryParams = stub.getQueryParams();
		// if (queryParams != null) {
		// for (Entry<String, String> e : queryParams.entrySet()) {
		// mappingBuilder.withQueryParam(e.getKey(), equalTo(e.getValue()));
		// }
		// }
		//
		// Map<String, String> headers = stub.getHeaders();
		// if (headers != null) {
		// for (Entry<String, String> e : headers.entrySet()) {
		// mappingBuilder.withHeader(e.getKey(), equalTo(e.getValue()));
		// }
		// }
		HttpServletRequest req = mockHttpRequest(queryParams);
		when(req.getMethod()).thenReturn("POST");

		stubFor(mappingBuilder.willReturn(stub.getHandler().handle(req)));
	}

	public void reset() {

		WireMock.reset();
	}

	public HttpServletRequest mockHttpRequest() {
		return mockHttpRequest(null);
	}

	public HttpServletRequest mockHttpRequest(Map<String, String> reqParams) {
		return mockHttpRequest(reqParams, null);
	}

	public HttpServletRequest mockHttpRequest(Map<String, String> reqParams, Map<String, Object> sessionAttributes) {

		HttpServletRequest req = mock(HttpServletRequest.class);

		if (reqParams != null) {
			for (Entry<String, String> e : reqParams.entrySet()) {
				when(req.getParameter(e.getKey())).thenReturn(e.getValue());
			}
		}

		HttpSession session = mock(HttpSession.class);
		sessionDataMap = new HashMap<>();
		if (sessionAttributes != null) {
			sessionDataMap.putAll(sessionAttributes);
		}
		when(req.getSession()).thenReturn(session);
		when(session.getAttribute(anyString())).thenAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String key = (String) invocation.getArguments()[0];
				return sessionDataMap.get(key);
			}
		});

		final Map<String, Object> requestAttributes = new HashMap<String, Object>();

		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				String name = (String) arguments[0];
				Object value = arguments[1];
				requestAttributes.put(name, value);
				return null;
			}
		}).when(req).setAttribute(anyString(), any());

		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				String name = (String) arguments[0];
				return requestAttributes.get(name);
			}
		}).when(req).getAttribute(anyString());

		ServletContext sctx = mock(ServletContext.class);
		when(sctx.getContextPath()).thenReturn(contextPath);
		when(req.getServletContext()).thenReturn(sctx);

		return req;
	}

	public HttpServletResponse mockHttpResponse(final String redirectUrl) throws IOException {
		HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
		Mockito.when(res.encodeRedirectURL(Mockito.anyString())).thenReturn(redirectUrl);

		Mockito.doAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String url = (String) invocation.getArguments()[0];
				Assert.assertEquals(redirectUrl, url);
				return null;
			}
		}).when(res).sendRedirect(Mockito.anyString());
		return res;
	}

	public static class RestCallStub {
		private String url;
		private String httpMethod;
		private RestCallHandler handler;
		private Map<String, String> queryParams;
		private Map<String, String> headers;

		public RestCallStub(String httpMethod, String url) {
			this.httpMethod = httpMethod;
			this.url = url;
		}

		public RestCallHandler getHandler() {
			return handler;
		}

		public void setHandler(RestCallHandler handler) {
			this.handler = handler;
		}

		public Map<String, String> getQueryParams() {
			return queryParams;
		}

		public void setQueryParams(Map<String, String> queryParams) {
			this.queryParams = queryParams;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getHttpMethod() {
			return httpMethod;
		}

		public void setHttpMethod(String httpMethod) {
			this.httpMethod = httpMethod;
		}
	}

	public static interface RestCallHandler {
		public ResponseDefinitionBuilder handle(HttpServletRequest req) throws Exception;

	}
}