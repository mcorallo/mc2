package it.mcsquared.engine.auth.server.utils;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.auth.server.stub.Database;
import it.mcsquared.engine.security.Mc2Encryption;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.JWT.Builder;
import org.apache.oltu.oauth2.jwt.io.JWTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenGenerator {
	private static final Logger logger = LoggerFactory.getLogger(TokenGenerator.class);
	private static long tokenDuration = 1 * 60 * 60 * 1000;
	private Mc2Encryption encryption;

	public TokenGenerator(Mc2Engine engine) throws Exception {
		String property = "oauth2.token.duration";
		String tokenDurationString = engine.getSystemProperty(property);
		if (tokenDurationString != null && !tokenDurationString.isEmpty()) {
			logger.info("Initializing with {}={}", property, tokenDurationString);
			try {
				tokenDuration = Long.parseLong(tokenDurationString);
			} catch (NumberFormatException e) {
				logger.info("the read property is not a number, trying to evaluate the expression");
				ScriptEngineManager mgr = new ScriptEngineManager();
				ScriptEngine jse = mgr.getEngineByName("JavaScript");
				tokenDuration = (long) jse.eval(tokenDurationString);
			}
		}

		encryption = new Mc2Encryption(engine.getConfDir());
	}

	public String generateAccessToken(String clientId, String username) throws Exception {
		OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
		String accessToken = oauthIssuerImpl.accessToken();//FIXME [mc2] aggiungere la scadenza del AT: 1h (box.com)
		String refreshToken = oauthIssuerImpl.refreshToken();//FIXME [mc2] aggiungere la scadenza del RT: 60gg (box.com)

		long issuedAt = System.currentTimeMillis();

		// all claims are optional
		Builder jwtBuilder = new Builder();
		// jwtBuilder.setClaimsSetIssuedAt(issuedAt);// The iat (issued at) claim identifies the time at which the JWT was issued
		// jwtBuilder.setClaimsSetExpirationTime(issuedAt + tokenDuration);// The exp (expiration time) claim identifies the expiration time on or after which the JWT MUST NOT be accepted for processing
		jwtBuilder.setClaimsSetAudience(username);// The aud (audience) claim identifies the recipients that the JWT is intended for
		jwtBuilder.setClaimsSetIssuer(clientId);// The iss (issuer) claim identifies the principal that issued the JWT
		jwtBuilder.setClaimsSetJwdId(accessToken);// The jti (JWT ID) claim provides a unique identifier for the JWT
		// jwtBuilder.setClaimsSetNotBefore(null);// The nbf (not before) claim identifies the time before which the JWT MUST NOT be accepted for processing
		jwtBuilder.setClaimsSetSubject(username);// The sub (subject) claim identifies the principal that is the subject of the JWT
		jwtBuilder.setClaimsSetType("bearer");
		// jwtBuilder.setClaimsSetCustomField("client_id", oauthRequest.getClientId());
		// jwtBuilder.setClaimsSetCustomField("username", oauthRequest.getUsername());
		jwtBuilder.setClaimsSetCustomField("scope", "read edit admin");
		jwtBuilder.setClaimsSetCustomField("refreshToken", refreshToken);

		jwtBuilder.setHeaderType("JWT");// If present, it is RECOMMENDED that its value be JWT to indicate that this object is a JWT
		// jwtBuilder.setHeaderAlgorithm(headerAlgorithm);
		// jwtBuilder.setHeaderContentType(headerContentType);//In the normal case in which nested signing or encryption operations are not employed, the use of this Header Parameter is NOT RECOMMENDED

		JWT jwtToken = jwtBuilder.build();
		JWTWriter w = new JWTWriter();
		String tempToken = w.write(jwtToken);
		String signature = encryption.sign(tempToken);
		jwtBuilder.setSignature(signature);

		jwtToken = jwtBuilder.build();
		String signedToken = w.write(jwtToken);
		Database.addRefreshToken(clientId, username, refreshToken);
		return signedToken;
	}

	public String generateAccessToken(OAuthTokenRequest oauthRequest) throws Exception {
		return generateAccessToken(oauthRequest.getClientId(), oauthRequest.getUsername());
	}

	public String generateAccessToken(OAuthAuthzRequest oauthRequest) throws Exception {
		return generateAccessToken(oauthRequest.getClientId(), oauthRequest.getParam("username"));
	}
}
