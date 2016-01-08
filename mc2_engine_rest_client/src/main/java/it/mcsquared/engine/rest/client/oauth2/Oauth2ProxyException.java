package it.mcsquared.engine.rest.client.oauth2;

@SuppressWarnings("serial")
public class Oauth2ProxyException extends RuntimeException {

	public Oauth2ProxyException(String message) {
		super(message);
	}

	public Oauth2ProxyException(Throwable e) {
		super(e);
	}

	public Oauth2ProxyException(String message, Throwable e) {
		super(message, e);
	}

}