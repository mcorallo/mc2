package it.mcsquared.engine.util;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class CryptUtils {

	public static String encodeSha1Hex(String text) {
		return DigestUtils.sha1Hex(text);
	}

	public static String encodeSha1WithSalt(String text1, String text2, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String hash1 = CryptUtils.encodeSha1WithSalt(text1, salt);
		String hash2 = CryptUtils.encodeSha1WithSalt(text2, salt);
		String hash = CryptUtils.encodeSha1WithSalt(hash1 + hash2, salt);
		return hash;
	}

	public static String encodeSha1WithSaltRight(String text1, String hash2, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String hash1 = CryptUtils.encodeSha1WithSalt(text1, salt);
		String hash = CryptUtils.encodeSha1WithSalt(hash1 + hash2, salt);
		return hash;
	}

	public static String encodeSha1WithSalt(String text, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = 50;
		char[] chars = text.toCharArray();
		byte[] saltBytes = salt.getBytes();

		PBEKeySpec spec = new PBEKeySpec(chars, saltBytes, iterations, 64 * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return iterations + ":" + toHex(saltBytes) + ":" + toHex(hash);
	}

	private static String toHex(byte[] array) throws NoSuchAlgorithmException {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
	}

	public static String generateRandomString(int length) {
		return RandomStringUtils.randomAlphanumeric(length);
	}

}
