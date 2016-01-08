package it.mcsquared.engine.web.filter;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.web.StartupListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter("/*")
public class CorsFilter implements Filter {

	private static boolean enabled = false;

	private List<String> allowedDomains;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletResponse response = (HttpServletResponse) res;

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST,GET,PUT,OPTIONS,DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
		chain.doFilter(req, res);

		// try {
		// String origin = request.getHeader("Origin");
		// if (enabled && origin != null && !origin.isEmpty()) {
		// String allowedOrigin = getAllowedOrigin(origin);
		// if (allowedOrigin != null) {
		// // cross domain (in jquery mettere crossDomain: true nell'oggetto ajax)
		//
		// // w3c note (http://www.w3.org/TR/enabled/#access-control-allow-origin-response-header):
		// // In practice the origin-list-or-null production is more constrained.
		// // Rather than allowing a space-separated list of origins, it is either a single origin or the string "null".
		// // - mcorallo: based on the w3c recommendation, we compare the received origin against the list of allowed domains and optionally authorize it
		// response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
		// // TODO [enabled]: verificare quali header restituire e con quali valori
		// response.setHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
		// response.setHeader("Access-Control-Allow-Credentials", "true");
		// response.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS, HEAD");
		// response.setHeader("Access-Control-Max-Age", "1209600");
		//
		// Enumeration<String> reqHead = request.getHeaders("Access-Control-Request-Headers");
		// if (reqHead != null) {
		// while (reqHead.hasMoreElements()) {
		// String h = (String) reqHead.nextElement();
		// response.setHeader("Access-Control-Allow-Headers", h);
		// }
		// }
		// logger.debug("rest response headers set, origin allowed: {}", origin);
		// } else {
		//
		// logger.debug("rest response headers not set, origin not allowed: {}", origin);
		// }
		//
		// }
		// } catch (Exception e) {
		// logger.error("", e);
		// }
	}

	private String getAllowedOrigin(String origin) {

		for (String d : allowedDomains) {
			if (d.equals(origin)) {
				return origin;
			}
		}
		return null;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		try {
			it.mcsquared.engine.web.InitListener.registerStartupListener(config.getServletContext(), new StartupListener() {

				@Override
				public void onSystemInitialized(Mc2Engine engine) {
					String[] allowedDomains = engine.getSystemProperties("cors.domains");
					CorsFilter.this.allowedDomains = Arrays.asList(allowedDomains);
					Logger logger = LoggerFactory.getLogger(CorsFilter.class);
					logger.info("CorsFilter initialized");
				}

			});
		} catch (Exception e) {
			e.printStackTrace();// not sure if the logger has been initialized
		}

	}

	@Override
	public void destroy() {
		// do nothing
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		CorsFilter.enabled = enabled;
	}

}
