package it.mcsquared.engine.web;

@SuppressWarnings("serial")
public class StartupException extends RuntimeException {

	public StartupException() {
	}

	public StartupException(String message) {
		super(message);
	}

	public StartupException(Exception exception) {
		super(exception);
	}

}
