package it.mcsquared.engine.web.filter;

import it.mcsquared.engine.web.Mc2WebEngine;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter("/*")
public class GZIPFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(GZIPFilter.class);

	@Override
	public void init(FilterConfig config) throws ServletException {
		logger.info("GZIPFilter initialized");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (Mc2WebEngine.getEngine(request.getServletContext()).isLocalEnv()) {
			logger.debug("zipping...");
		}
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String acceptEncoding = req.getHeader("Accept-Encoding");
		if (acceptEncoding != null) {
			if (acceptEncoding.indexOf("gzip") >= 0) {
				String servletPath = req.getServletPath();
				if (!servletPath.endsWith(".jsp")) {
					GZIPHttpServletResponseWrapper gzipResponse = new GZIPHttpServletResponseWrapper(res);
					chain.doFilter(request, gzipResponse);
					gzipResponse.finish();
					return;
				}
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
