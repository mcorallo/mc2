package it.mcsquared.engine.util;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

public class CryptUtilsTest {

	@Test
	public void testEncodeSha1Hex() {
		String encoded = CryptUtils.encodeSha1Hex("aaa");
		assertEquals("7e240de74fb1ed08fa08d38063f6a6a91462a815", encoded);
	}

	@Test
	public void testEncodeSha1WithSalt1() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String encoded = CryptUtils.encodeSha1WithSalt("aaa", "123");
		assertEquals("50:313233:017a59c7243053e6f656d6aaf4d31139dc87ebbed2c1e0fef7a0af873790b1bb38d022f1422087428894810b440cc172d2f878ef6e76c247ba0f5b125cf40920", encoded);
	}

	@Test
	public void testEncodeSha1WithSalt2() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String encoded = CryptUtils.encodeSha1WithSalt("aaa", "bbb", "123");
		assertEquals("50:313233:9328afde5d2d964fd4ab8c98956c3d467bb8005106e6cdff84fe47eb0016f53bfef998b9e8d4af8020541234b4b97d1a207f81828eb7d9530d4dba834e536645", encoded);
	}

	@Test
	public void testEncodeSha1WithSaltRight() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String encoded = CryptUtils.encodeSha1WithSaltRight("aaa", "bbb", "123");
		assertEquals("50:313233:c8d7b8de323168e61bbea7bc4428972bfd8bfc9e0221473675d7a52e6212545c7235335febc574152f3aa3b8514a5607d22268b18e5b03e31bc6dbd15b6ad23f", encoded);
	}

	@Test
	public void testGenerateRandomString() {
		int size = 10;
		String encoded = CryptUtils.generateRandomString(size);
		assertEquals(size, encoded.length());
	}

}
