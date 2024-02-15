package dev.nipafx.ginevra.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHA256 {

	private static final MessageDigest SHA_256;

	static {
		try {
			SHA_256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("All implementations of the Java platform are required to support SHA-256", ex);
		}
	}


	public static String hash(String string) {
		synchronized (SHA_256) {
			SHA_256.update(string.getBytes());
			var hash = Base64.getEncoder().encodeToString(SHA_256.digest());
			return hash.replaceAll("\\W", "");
		}
	}

}
