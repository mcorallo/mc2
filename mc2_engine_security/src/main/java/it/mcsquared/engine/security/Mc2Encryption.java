package it.mcsquared.engine.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;

public class Mc2Encryption {

	private static final String SIGNATURE_ALGORITHM = "MD5WithRSA";
	private static final String ALGORITHM = "RSA";

	public static final String PRIVATE_KEY_FILE = "auth.server.private.pem";
	public static final String PUBLIC_KEY_FILE = "auth.server.public.pem";

	private String parentFolder;
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public Mc2Encryption(String parentFolder) throws Exception {
		if (parentFolder == null) {
			parentFolder = ".";
		}
		this.parentFolder = parentFolder;

		loadKeys();
	}

	public void generateKeys() throws Exception {
		if (parentFolder == null) {
			parentFolder = ".";
		}
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
		keyGen.initialize(1024);

		KeyPair keys = keyGen.generateKeyPair();

		File privateKeyFile = new File(parentFolder, PRIVATE_KEY_FILE);
		File publicKeyFile = new File(parentFolder, PUBLIC_KEY_FILE);

		privateKeyFile.createNewFile();
		publicKeyFile.createNewFile();

		try (ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publicKeyFile))) {
			publicKeyOS.writeObject(keys.getPublic());
		}

		try (ObjectOutputStream privateKeyOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile))) {
			privateKeyOS.writeObject(keys.getPrivate());
		}
		loadKeys();
	}

	public void loadKeys() throws IOException, ClassNotFoundException, FileNotFoundException {
		File privateKeyFile = new File(parentFolder, PRIVATE_KEY_FILE);
		if (privateKeyFile.exists()) {

			try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(privateKeyFile))) {
				privateKey = (PrivateKey) inputStream.readObject();
			}
		}

		File publicKeyFile = new File(parentFolder, PUBLIC_KEY_FILE);
		if (publicKeyFile.exists()) {
			try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(publicKeyFile))) {
				publicKey = (PublicKey) inputStream.readObject();
			}
		}
	}

	public byte[] encrypt(String text) throws Exception {
		byte[] cipherText = null;
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		cipherText = cipher.doFinal(text.getBytes());
		return cipherText;
	}

	public String decrypt(byte[] text) throws Exception {
		byte[] dectyptedText = null;
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		dectyptedText = cipher.doFinal(text);
		return new String(dectyptedText);
	}

	public String sign(String text) throws Exception {
		Signature dsa = Signature.getInstance(SIGNATURE_ALGORITHM);
		dsa.initSign(privateKey);

		try (InputStream fis = IOUtils.toInputStream(text);//
				BufferedInputStream bufin = new BufferedInputStream(fis)) {
			byte[] buffer = new byte[1024];
			int len;
			while (bufin.available() != 0) {
				len = bufin.read(buffer);
				dsa.update(buffer, 0, len);
			}
		}

		return DatatypeConverter.printBase64Binary(dsa.sign());
	}

	public boolean verifySignature(String text, String signature) throws Exception {

		Signature dsa = Signature.getInstance(SIGNATURE_ALGORITHM);
		dsa.initVerify(publicKey);

		try (ByteArrayInputStream bufin = new ByteArrayInputStream(text.getBytes())) {

			byte[] buffer = new byte[1024];
			int len;
			while (bufin.available() != 0) {
				len = bufin.read(buffer);
				dsa.update(buffer, 0, len);
			}
		}
		return dsa.verify(DatatypeConverter.parseBase64Binary(signature));
	}

	public void deleteFiles() {
		File privateKeyFile = new File(parentFolder, PRIVATE_KEY_FILE);
		privateKeyFile.delete();
		File publicKeyFile = new File(parentFolder, PUBLIC_KEY_FILE);
		publicKeyFile.delete();
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public static void main(String[] args) throws Exception {
		Mc2Encryption e = new Mc2Encryption(".");
		e.generateKeys();
	}

}