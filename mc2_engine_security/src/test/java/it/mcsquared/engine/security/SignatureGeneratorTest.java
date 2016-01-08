package it.mcsquared.engine.security;

import static org.junit.Assert.*;
import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.test.GenericTest;

import org.junit.Test;

public class SignatureGeneratorTest extends GenericTest {
	private Mc2Encryption mc2Encryption;

	@Override
	protected void localSetup() throws Exception {
		Mc2Engine engine = (Mc2Engine) getEngine();
		String confDir = engine.getConfDir();

		mc2Encryption = new Mc2Encryption(confDir);
		mc2Encryption.generateKeys();
	}

	@Override
	protected void localTearDown() throws Exception {
		mc2Encryption.deleteFiles();
	}

	@Test
	public void signTest() throws Exception {
		String signature = mc2Encryption.sign("test-string");
		assertNotNull(signature);
		System.out.println(signature);
	}
}
