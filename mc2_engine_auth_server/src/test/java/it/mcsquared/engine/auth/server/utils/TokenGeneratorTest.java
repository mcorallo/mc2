package it.mcsquared.engine.auth.server.utils;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.security.Mc2Encryption;
import it.mcsquared.engine.test.GenericTest;

import org.junit.Test;

public class TokenGeneratorTest extends GenericTest {

	private Mc2Encryption encryptionUtils;

	@Override
	protected void localSetup() throws Exception {
		encryptionUtils = new Mc2Encryption(confDir);
		encryptionUtils.generateKeys();
	}

	@Override
	protected void localTearDown() throws Exception {
		encryptionUtils.deleteFiles();
	}

	@Test
	public void generateAccessTokenTest() throws Exception {
		TokenGenerator tokenGenerator = new TokenGenerator((Mc2Engine) getEngine());
		String token = tokenGenerator.generateAccessToken("cid", "un");
		System.out.println(token);
	}
}
