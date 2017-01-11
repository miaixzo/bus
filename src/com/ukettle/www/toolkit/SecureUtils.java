package com.ukettle.www.toolkit;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class SecureUtils {

	private static AlgorithmParameterSpec iv = null;
	private static Key key = null;
	private static String encode = "UTF-8";

	private static byte[] byte_key = null;
	private static byte[] byte_iv = null;

	public SecureUtils(String strkey, String striv, String encoding)
			throws Exception {
		byte_key = strkey.getBytes();
		byte_iv = striv.getBytes();
		if ((encoding != null) && (!"".equals(encoding))) {
			encode = encoding;
		}
		DESKeySpec keySpec = new DESKeySpec(byte_key);

		iv = new IvParameterSpec(byte_iv);

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		key = keyFactory.generateSecret(keySpec);
	}

	public String encode(String data) throws Exception {
		Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

		enCipher.init(1, key, iv);
		byte[] pasByte = enCipher.doFinal(data.getBytes(encode));
		BASE64Encoder base64Encoder = new BASE64Encoder();
		return base64Encoder.encode(pasByte);
	}

	public String decode(String data) throws Exception {
		Cipher deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

		deCipher.init(2, key, iv);
		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] pasByte = deCipher.doFinal(base64Decoder.decodeBuffer(data));
		return new String(pasByte, encode);
	}

}