/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.vending.licensing;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.android.vending.licensing.util.Base64;
import com.android.vending.licensing.util.Base64DecoderException;

/**
 * An Obfuscator that uses AES to encrypt data.
 */
public class AESObfuscator implements Obfuscator {
	private static final String UTF8 = "UTF-8";
	private static final String KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final byte[] IV = {16, 74, 71, -80, 32, 101, -47, 72, 117, -14, 0, -29, 70, 65, -12, 74};
	private static final String header = "com.android.vending.licensing.AESObfuscator-1|";

	private Cipher mEncryptor;
	private Cipher mDecryptor;

	/**
	 * @param salt
	 *            an array of random bytes to use for each (un)obfuscation
	 * @param applicationId
	 *            application identifier, e.g. the package name
	 * @param deviceId
	 *            device identifier. Use as many sources as possible to create
	 *            this unique identifier.
	 */
	public AESObfuscator(final byte[] salt, final String applicationId, final String deviceId) {
		try {
			final SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
			final KeySpec keySpec = new PBEKeySpec((applicationId + deviceId).toCharArray(), salt, 1024, 256);
			final SecretKey tmp = factory.generateSecret(keySpec);
			final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			mEncryptor = Cipher.getInstance(CIPHER_ALGORITHM);
			mEncryptor.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(IV));
			mDecryptor = Cipher.getInstance(CIPHER_ALGORITHM);
			mDecryptor.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(IV));
		} catch (final GeneralSecurityException e) {
			// This can't happen on a compatible Android device.
			throw new RuntimeException("Invalid environment", e);
		}
	}

	public String obfuscate(final String original) {
		if (original == null) {
			return null;
		}
		try {
			// Header is appended as an integrity check
			return Base64.encode(mEncryptor.doFinal((header + original).getBytes(UTF8)));
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("Invalid environment", e);
		} catch (final GeneralSecurityException e) {
			throw new RuntimeException("Invalid environment", e);
		}
	}

	public String unobfuscate(final String obfuscated) throws ValidationException {
		if (obfuscated == null) {
			return null;
		}
		try {
			final String result = new String(mDecryptor.doFinal(Base64.decode(obfuscated)), UTF8);
			// Check for presence of header. This serves as a final integrity
			// check, for cases
			// where the block size is correct during decryption.
			final int headerIndex = result.indexOf(header);
			if (headerIndex != 0) {
				throw new ValidationException("Header not found (invalid data or key)" + ":" + obfuscated);
			}
			return result.substring(header.length(), result.length());
		} catch (final Base64DecoderException e) {
			throw new ValidationException(e.getMessage() + ":" + obfuscated);
		} catch (final IllegalBlockSizeException e) {
			throw new ValidationException(e.getMessage() + ":" + obfuscated);
		} catch (final BadPaddingException e) {
			throw new ValidationException(e.getMessage() + ":" + obfuscated);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("Invalid environment", e);
		}
	}
}