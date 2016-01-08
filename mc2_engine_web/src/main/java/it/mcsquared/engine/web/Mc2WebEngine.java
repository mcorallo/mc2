package it.mcsquared.engine.web;

import it.mcsquared.engine.Mc2Engine;

import javax.servlet.ServletContext;

public class Mc2WebEngine {
	private static final String _MC2_ENGINE = "_MC2_ENGINE";

	public static void init(ServletContext context, Mc2Engine engine) {
		context.setAttribute(_MC2_ENGINE, engine);
	}

	public static Mc2Engine getEngine(ServletContext context) {
		return (Mc2Engine) context.getAttribute(_MC2_ENGINE);
	}
}
