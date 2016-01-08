package it.mcsquared.engine.security;

@SuppressWarnings("serial")
public class SecurityException extends RuntimeException {

	public SecurityException() {
	}

	public SecurityException(String message) {
		super(message);
	}

	public SecurityException(Exception exception) {
		super(exception);
	}
}
