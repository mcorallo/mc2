package it.mcsquared.engine.web;

import it.mcsquared.engine.Mc2Engine;

public interface StartupListener {

	void onSystemInitialized(Mc2Engine engine) throws Exception;
}