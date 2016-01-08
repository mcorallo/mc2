package it.mcsquared.engine.security;

import static org.junit.Assert.*;
import it.mcsquared.engine.test.GenericTest;

import java.io.File;

import org.junit.Test;

public class EncryptionUtilTest extends GenericTest {

	private String parentFolder = ".";
	private Mc2Encryption encryptionUtils;

	@Override
	protected void localSetup() throws Exception {
		encryptionUtils = new Mc2Encryption(parentFolder);
	}

	@Override
	protected void localTearDown() throws Exception {
		encryptionUtils.deleteFiles();
	}

	@Test
	public void encryptDecryptTest() throws Exception {
		encryptionUtils.generateKeys();

		File privateKeyFile = new File(parentFolder, Mc2Encryption.PRIVATE_KEY_FILE);
		assertTrue(privateKeyFile.exists());
		File publicKeyFile = new File(parentFolder, Mc2Encryption.PUBLIC_KEY_FILE);
		assertTrue(publicKeyFile.exists());

		String originalText = "Text to be encrypted ";

		byte[] cipherText = encryptionUtils.encrypt(originalText);
		String plainText = encryptionUtils.decrypt(cipherText);
		assertEquals(originalText, plainText);
	}
}
