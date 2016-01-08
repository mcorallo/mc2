package it.mcsquared.engine.auth.server.auth;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Base64;

public class AccessTokenTest {
	// @Test
	// public void md5GeneratorTest() throws Exception {
	// OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
	// String accessToken = oauthIssuerImpl.accessToken();
	// System.out.println(accessToken);
	// }
	//
	// @Test
	// public void uuidGeneratorTest() throws Exception {
	// OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new UUIDValueGenerator());
	// String accessToken = oauthIssuerImpl.accessToken();
	// System.out.println(accessToken);
	// }

//	@Test
//	public void jwtTest() throws Exception {
//
//		Builder jwtBuilder = new Builder();
//		jwtBuilder.setClaimsSetExpirationTime(1);
//		jwtBuilder.setHeaderType("a");
//		JWT jwtToken = jwtBuilder.build();
//		JWTWriter w = new JWTWriter();
//		String token = w.write(jwtToken);
//		System.out.println(jwtToken);
//		System.out.println(token);
//		System.out.println("***");
//		System.out.println("***");
//
//		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//
//		Signature s = Signature.getInstance("SHA1withDSA", "SUN");
//		s.initSign(get());
//		s.update(token.getBytes());
//		byte[] sign = s.sign();
//		System.out.println(new String(sign));
//		// jwtBuilder.setSignature("aaa");
//		//
//		//
//		//
//		//
//		//
//		// JWTReader r = new JWTReader();
//		// JWT jwtTokenRead = r.read(token);
//		// assertEquals(token, jwtTokenRead.getRawString());
//
//	}

	public PrivateKey get() throws Exception {
		// Remove the first and last lines
		String privKeyPEM = IOUtils.toString(this.getClass().getResourceAsStream("/privkey.pem"))//
				.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
		privKeyPEM = privKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
		System.out.println(privKeyPEM);
		System.out.println("AAA\n");

		// Base64 decode the data

		byte[] encoded = Base64.decode(privKeyPEM);

		// PKCS8 decode the encoded RSA private key

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey privKey = kf.generatePrivate(keySpec);

		// Display the results

		System.out.println(privKey);
		System.out.println("BBB\n");
		return privKey;
	}
}
