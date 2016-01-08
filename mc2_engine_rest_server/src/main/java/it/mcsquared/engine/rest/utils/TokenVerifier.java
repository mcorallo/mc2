package it.mcsquared.engine.rest.utils;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.security.Mc2Encryption;
import it.mcsquared.engine.web.StartupException;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.io.JWTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenVerifier {
	private static final String OAUTH2_CLIENT_ID = "oauth2.client.id";
	private static final Logger logger = LoggerFactory.getLogger(TokenVerifier.class);
	private static String clientId;
	private Mc2Encryption mc2Encryption;

	public TokenVerifier(Mc2Engine engine) throws Exception {
		clientId = engine.getSystemProperty(OAUTH2_CLIENT_ID);
		if (clientId == null || clientId.isEmpty()) {
			throw new StartupException("Oauth2 client id not set. Check the system property " + OAUTH2_CLIENT_ID);
		}

		mc2Encryption = new Mc2Encryption(engine.getConfDir());
	}

	public boolean verify(String accessToken) throws Exception {
		JWTReader r = new JWTReader();
		JWT token = r.read(accessToken);
		String signature = token.getSignature();
		String completeToken = token.getRawString();
		String tempToken = completeToken.replace(signature, "null");
		boolean valid = mc2Encryption.verifySignature(tempToken, signature);
		logger.debug("token validation {}: {}", valid, completeToken);
		if (!valid) {
			return false;
		}

		long expirationTime = token.getClaimsSet().getExpirationTime();
		valid = expirationTime == 0 || System.currentTimeMillis() <= expirationTime;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		logger.debug("token expiration {}: {}", valid, sdf.format(new Date(expirationTime)));
		if (!valid) {
			return false;
		}

		String issuer = token.getClaimsSet().getIssuer();
		valid = clientId.equals(issuer);
		logger.debug("token issuer {}: {}", valid, issuer);
		if (!valid) {
			return false;
		}

		return true;
	}
}
