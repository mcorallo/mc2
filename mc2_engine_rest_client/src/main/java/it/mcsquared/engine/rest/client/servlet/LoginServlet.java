package it.mcsquared.engine.rest.client.servlet;

import it.mcsquared.engine.rest.client.oauth2.Oauth2Proxy;
import it.mcsquared.engine.rest.client.oauth2.Oauth2ProxyException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthRuntimeException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		APIProxyServlet.initOauth2Proxy(config.getServletContext());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, Object> data = new HashMap<>();
		try {
			logger.debug("processing login request...");
			req.getSession().invalidate();

			String username = req.getParameter("username");
			String password = req.getParameter("password");

			String token = Oauth2Proxy.authenticate(req, username, password);
			data.put("token", token);
			data.put("success", true);

		} catch (Oauth2ProxyException | OAuthProblemException | OAuthSystemException | OAuthRuntimeException e) {
			data.put("error", true);
			data.put("message", e.toString());
			// resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (Exception e) {
			logger.error("", e);
			data.put("error", true);
			data.put("message", e.toString());
			// resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		try (PrintWriter writer = resp.getWriter()) {
			writer.write(new JSONObject(data).toString());
			writer.flush();

		}
	}

}