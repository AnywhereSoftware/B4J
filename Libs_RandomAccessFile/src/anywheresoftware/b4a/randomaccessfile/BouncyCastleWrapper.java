
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
 
 package anywheresoftware.b4a.randomaccessfile;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;

import anywheresoftware.b4a.BA.Hide;

@Hide
public class BouncyCastleWrapper {
	private static final String KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final byte[] SALT = new byte[] {12,54,23,45,23,52,12};
	private static final byte[] IV =  { 116, 13, 72, -50, 77, 45, -3, -72, -117, 32, 23, 19, 72, 21, 111, 22 };
	
	public static byte[] encryptWithLWCrypto(byte[] data, String password)
			throws Exception
	{
		PKCS12ParametersGenerator pGen = new PKCS12ParametersGenerator(new SHA1Digest());
		char[] passwordChars = password.toCharArray();
		final byte[] pkcs12PasswordBytes = PBEParametersGenerator
				.PKCS12PasswordToBytes(passwordChars);
		pGen.init(pkcs12PasswordBytes, SALT, 1024);
		CBCBlockCipher aesCBC = new CBCBlockCipher(new AESEngine());
		ParametersWithIV aesCBCParams = new ParametersWithIV(pGen.generateDerivedParameters(256), IV);
		aesCBC.init(true, aesCBCParams);
		PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(aesCBC,
				new PKCS7Padding());
		aesCipher.init(true,(pGen.generateDerivedParameters(256)));
		byte[] plainTemp = new byte[aesCipher.getOutputSize(data.length)];
		int offset = aesCipher.processBytes(data, 0, data.length, plainTemp, 0);
		int last = aesCipher.doFinal(plainTemp, offset);
		final byte[] plain = new byte[offset + last];
		System.arraycopy(plainTemp, 0, plain, 0, plain.length);
		return plain;
	}
	public static byte[] decryptWithLWCrypto(byte[] data, String password)
			throws Exception
	{
		PKCS12ParametersGenerator pGen = new PKCS12ParametersGenerator(new SHA1Digest());
		char[] passwordChars = password.toCharArray();
		final byte[] pkcs12PasswordBytes = PBEParametersGenerator
				.PKCS12PasswordToBytes(passwordChars);
		pGen.init(pkcs12PasswordBytes, SALT, 1024);
		CBCBlockCipher aesCBC = new CBCBlockCipher(new AESEngine());
		ParametersWithIV aesCBCParams = new ParametersWithIV(pGen.generateDerivedParameters(256), IV);
		aesCBC.init(false, aesCBCParams);
		PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(aesCBC,
				new PKCS7Padding());
		byte[] plainTemp = new byte[aesCipher.getOutputSize(data.length)];
		int offset = aesCipher.processBytes(data, 0, data.length, plainTemp, 0);
		int last = aesCipher.doFinal(plainTemp, offset);
		final byte[] plain = new byte[offset + last];
		System.arraycopy(plainTemp, 0, plain, 0, plain.length);
		return plain;
	}
}
