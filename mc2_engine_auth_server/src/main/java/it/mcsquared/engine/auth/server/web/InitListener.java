package it.mcsquared.engine.auth.server.web;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.rest.provider.SecurityFilter;
import it.mcsquared.engine.web.StartupException;
import it.mcsquared.engine.web.StartupListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class InitListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			final ServletContext context = event.getServletContext();
			it.mcsquared.engine.web.InitListener.registerStartupListener(context, new StartupListener() {

				@Override
				public void onSystemInitialized(Mc2Engine engine) {
					try {
						Database.init(engine);
						SecurityFilter.init(engine);
						Logger logger = LoggerFactory.getLogger(InitListener.class);
						logger.info("Engine auth server (" + context.getContextPath().replace("/", "") + ") initialized");
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

			});
		} catch (Exception e) {
			throw new StartupException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

}
