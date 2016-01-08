package it.mcsquared.engine.web;

import it.mcsquared.engine.Constants.InitParams;
import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.web.filter.CorsFilter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class InitListener implements ServletContextListener {

	public static final String STARTUP_LISTENER_REGISTER = "STARTUP_LISTENER_REGISTER";

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			ServletContext context = event.getServletContext();

			String confDir = System.getProperty("mc2.conf.dir." + context.getContextPath().replace("/", ""));
			if (confDir == null || confDir.isEmpty()) {
				confDir = context.getInitParameter(InitParams.CONF_DIR);
			}

			Mc2Engine engine = Mc2Engine.init(context.getContextPath(), confDir);
			Mc2WebEngine.init(context, engine);

			StartupListenerRegister startupListenerRegister = new StartupListenerRegister(engine);
			context.setAttribute(STARTUP_LISTENER_REGISTER, startupListenerRegister);

			String corsEnabled = engine.getSystemProperty("cors.enabled");
			if (corsEnabled != null && Boolean.parseBoolean(corsEnabled)) {
				CorsFilter.setEnabled(true);
			}

			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.error("Init complete.");

			startupListenerRegister.notifyListeners();
		} catch (Exception ex) {
			throw new StartupException(ex);
		}
	}

	public static void registerStartupListener(final ServletContext context, final StartupListener startupListener) throws Exception {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					int maxRun = 100;
					int run = 0;
					StartupListenerRegister startupListenerRegister = null;
					while (startupListenerRegister == null && run < maxRun) {
						run++;
						startupListenerRegister = (StartupListenerRegister) context.getAttribute(STARTUP_LISTENER_REGISTER);
						Thread.sleep(500);
					}

					if (startupListenerRegister != null) {
						startupListenerRegister.registerStartupListener(startupListener);
						System.out.println("startup listener registered: " + startupListener);
					} else {
						System.out.println("error during startup listener registration: " + run);
					}
				} catch (Exception e) {
					throw new StartupException(e);
				}
			}
		};
		t.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

}
