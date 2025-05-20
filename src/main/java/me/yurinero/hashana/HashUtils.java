package me.yurinero.hashana;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/*
* Utility class implementing the Google Guava hashing functions so we may re-use them in the controllers.
 */

public final class HashUtils {
	private HashUtils() {} // Prevent instantiation

	public static HashFunction getHashFunction(String algorithm) {
		return switch (algorithm.toUpperCase()) {
			case "SHA256" -> Hashing.sha256();
			case "SHA384" -> Hashing.sha384();
			case "SHA512" -> Hashing.sha512();
			case "MD5" -> Hashing.md5();
			case "SIPHASH24" -> Hashing.sipHash24();
			default -> throw new IllegalArgumentException("Unsupported algorithm");
		};
	}
}