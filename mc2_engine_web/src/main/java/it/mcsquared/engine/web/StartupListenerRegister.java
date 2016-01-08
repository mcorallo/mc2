package it.mcsquared.engine.web;

import it.mcsquared.engine.Mc2Engine;

import java.util.ArrayList;
import java.util.List;

public class StartupListenerRegister {

	private List<StartupListener> listeners = new ArrayList<>();

	private boolean initialized;

	private Mc2Engine engine;

	public StartupListenerRegister(Mc2Engine engine) {
		this.engine = engine;
	}

	void registerStartupListener(StartupListener listener) throws Exception {
		if (!initialized) {
			listeners.add(listener);
		} else {
			listener.onSystemInitialized(engine);
		}
	}

	synchronized void notifyListeners() throws Exception {
		initialized = true;
		for (StartupListener sl : listeners) {
			sl.onSystemInitialized(engine);
		}
	}

}