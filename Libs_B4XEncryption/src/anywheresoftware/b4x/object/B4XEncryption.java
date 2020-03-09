
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4x.object;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

/**
 * B4XCipher uses AES with a random salt and random initialization vector to encrypt the data.
 * The salt and IV are stored in the returned data. This encryption method is compatible with B4A B4XCipher and B4i Cipher objects.
 * Note that it depends on BouncyCastle.
 */
@ShortName("B4XCipher")
@Version(1.00f)
public class B4XEncryption {
	/**
	 * Encrypts the given data with the given password.
	 */
	public byte[] Encrypt(byte[] Data, String Password) throws Exception {
		SecureRandom rnd = new SecureRandom();
		byte[] salt = new byte[8];
		rnd.nextBytes(salt);
		byte[] iv = new byte[16];
		rnd.nextBytes(iv);
		PKCS5S2ParametersGenerator pGen = new PKCS5S2ParametersGenerator(new SHA1Digest());
		final byte[] pkcs12PasswordBytes = Password.getBytes("UTF8");
		pGen.init(pkcs12PasswordBytes, salt, 1024);
		CBCBlockCipher aesCBC = new CBCBlockCipher(new AESEngine());
		ParametersWithIV aesCBCParams = new ParametersWithIV(pGen.generateDerivedParameters(128), iv);
		aesCBC.init(true, aesCBCParams);
		PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(aesCBC,
				new PKCS7Padding());
		aesCipher.init(true,(pGen.generateDerivedParameters(128)));
		byte[] plainTemp = new byte[aesCipher.getOutputSize(Data.length)];
		int offset = aesCipher.processBytes(Data, 0, Data.length, plainTemp, 0);
		int last = aesCipher.doFinal(plainTemp, offset);
		final byte[] plain = new byte[offset + last + 24];
		System.arraycopy(salt, 0, plain, 0, 8);
		System.arraycopy(iv, 0, plain, 8, 16);
		System.arraycopy(plainTemp, 0, plain, 24, offset + last);
		return plain;
	}
	/**
	 * Decrypts the given data with the given password.
	 */
	public byte[] Decrypt(byte[] Data, String Password) throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
		byte[] salt = new byte[8];
		byte[] iv = new byte[16];
		System.arraycopy(Data, 0, salt, 0, 8);
		System.arraycopy(Data, 8, iv, 0, 16);
		PKCS5S2ParametersGenerator pGen = new PKCS5S2ParametersGenerator(new SHA1Digest());
		final byte[] pkcs12PasswordBytes = Password.getBytes("UTF8");
		pGen.init(pkcs12PasswordBytes, salt, 1024);
		CBCBlockCipher aesCBC = new CBCBlockCipher(new AESEngine());
		ParametersWithIV aesCBCParams = new ParametersWithIV(pGen.generateDerivedParameters(128), iv);
		aesCBC.init(false, aesCBCParams);
		PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(aesCBC,
				new PKCS7Padding());
		aesCipher.init(false,(pGen.generateDerivedParameters(128)));
		byte[] plainTemp = new byte[aesCipher.getOutputSize(Data.length - 24)];
		int offset = aesCipher.processBytes(Data, 24, Data.length - 24, plainTemp, 0);
		int last = aesCipher.doFinal(plainTemp, offset);
		final byte[] plain = new byte[offset + last];
		System.arraycopy(plainTemp, 0, plain, 0, plain.length);
		return plain;
	}
}
