/*
 * Hashana - A desktop utility for hashing and password generation.
 * Copyright (C) 2025 Yurinero <https://github.com/Yurinero>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.yurinero.hashana.utils;

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