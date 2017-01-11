package org.ukettle.www.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.lang.Validate;
import org.ukettle.www.exception.uKettleException;

/**
 * <p>
 * 支持SHA-1/MD5消息摘要的工具类. 返回ByteSource，可进一步被编码为Hex, Base64或UrlSafeBase64
 * </p>
 * 
 * @author Kimi Liu
 * @Date Apr 16, 2014
 * @Time 13:13:09
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class Random extends java.util.Random {

	private static final long serialVersionUID = -8535634573985693003L;

	public static final String LETTER = "0123456789ABCDEF";
	public static final String SECRET = "$link^5#";
	public static final String MD5 = "MD5";
	public static final String DES = "DES";
	public static final String SHA1 = "SHA-1";
	public static final int CODE_SIZE = 8;
	public static final int ITERATION = 1024;

	private static Random instance = null;
	private static SecretKey key = null;

	/**
	 * 生成随机的Byte[]作为salt.
	 * 
	 * @param numBytes
	 *            byte数组的大小
	 */
	public static byte[] generate(int numBytes) {
		Validate.isTrue(numBytes > 0,
				"numBytes argument must be a positive integer (1 or larger)",
				numBytes);
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[numBytes];
		random.nextBytes(bytes);
		return bytes;
	}

	/**
	 * 对输入字符串进行sha1散列.
	 */
	public static byte[] sha1(byte[] input) {
		return digest(input, SHA1, null, 1);
	}

	public static byte[] sha1(byte[] input, byte[] salt) {
		return digest(input, SHA1, salt, 1);
	}

	public static byte[] sha1(byte[] input, byte[] salt, int iterations) {
		return digest(input, SHA1, salt, iterations);
	}

	/**
	 * 对输入字符串进行md5散列.
	 */
	public static byte[] md5(byte[] input) {
		return digest(input, MD5, null, 1);
	}

	public static byte[] md5(byte[] input, byte[] salt) {
		return digest(input, MD5, salt, 1);
	}

	public static byte[] md5(byte[] input, byte[] salt, int iterations) {
		return digest(input, MD5, salt, iterations);
	}

	/**
	 * 对文件进行md5散列.
	 */
	public static byte[] md5(InputStream input) throws IOException {
		return digest(input, MD5);
	}

	/**
	 * 对文件进行sha1散列.
	 */
	public static byte[] sha1(InputStream input) throws IOException {
		return digest(input, SHA1);
	}

	/**
	 * 对字符串进行散列, 支持md5与sha1算法.
	 */
	private static byte[] digest(byte[] input, String algorithm, byte[] salt,
			int iterations) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			if (salt != null) {
				digest.update(salt);
			}
			byte[] result = digest.digest(input);
			for (int i = 1; i < iterations; i++) {
				digest.reset();
				result = digest.digest(result);
			}
			return result;
		} catch (GeneralSecurityException e) {
			throw uKettleException.unchecked(e);
		}
	}

	/**
	 * 对文件进行散列, 支持md5与sha1算法.
	 */
	private static byte[] digest(InputStream input, String type)
			throws IOException {
		try {
			MessageDigest digest = MessageDigest.getInstance(type);
			int bufferLength = 8 * 1024;
			byte[] buffer = new byte[bufferLength];
			int read = input.read(buffer, 0, bufferLength);

			while (read > -1) {
				digest.update(buffer, 0, read);
				read = input.read(buffer, 0, bufferLength);
			}
			return digest.digest();
		} catch (GeneralSecurityException e) {
			throw uKettleException.unchecked(e);
		}
	}

	/**
	 * 根据指定长度生成字母和数字的随机数
	 * 
	 * 0~9的ASCII为48~57, A~Z的ASCII为65~90, a~z的ASCII为97~122
	 */
	public static String character(int length) {
		StringBuilder sb = new StringBuilder();
		java.util.Random rand = new java.util.Random();
		// 随机用以下三个随机生成器
		java.util.Random randdata = new java.util.Random();
		int data = 0;
		for (int i = 0; i < length; i++) {
			int index = rand.nextInt(3);
			// 目的是随机选择生成数字，大小写字母
			switch (index) {
			case 0:
				// 仅仅会生成0~9
				data = randdata.nextInt(10);
				sb.append(data);
				break;
			case 1:
				// 保证只会产生65~90之间的整数
				data = randdata.nextInt(26) + 65;
				sb.append((char) data);
				break;
			case 2:
				// 保证只会产生97~122之间的整数
				data = randdata.nextInt(26) + 97;
				sb.append((char) data);
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * 根据指定长度生成纯数字的随机数
	 * 
	 */
	public static String number(int n) {
		if (n < 1 || n > 10) {
			throw new IllegalArgumentException("cannotrandom " + n
					+ " bitnumber");
		}
		Random ran = new Random();
		if (n == 1) {
			return String.valueOf(ran.nextInt(10));
		}
		int bitField = 0;
		char[] chs = new char[n];
		for (int i = 0; i < n; i++) {
			while (true) {
				int k = ran.nextInt(10);
				if ((bitField & (1 << k)) == 0) {
					bitField |= 1 << k;
					chs[i] = (char) (k + '0');
					break;
				}
			}
		}
		return new String(chs);
	}

	public static String random(int n) {
		if (n < 1 || n > 10) {
			throw new IllegalArgumentException("cannot random " + n
					+ " bit number");
		}
		Random ran = new Random();
		if (n == 1) {
			return String.valueOf(ran.nextInt(10));
		}
		int bitField = 0;
		char[] chs = new char[n];
		for (int i = 0; i < n; i++) {
			while (true) {
				int k = ran.nextInt(10);
				if ((bitField & (1 << k)) == 0) {
					bitField |= 1 << k;
					chs[i] = (char) (k + '0');
					break;
				}
			}
		}
		return new String(chs);
	}

	public static String md5(String value) {
		try {
			return hash(MessageDigest.getInstance(MD5), value);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String sha1(String value) {
		try {
			return hash(MessageDigest.getInstance(SHA1), value);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String hash(String type, String value) {
		try {
			return hash(MessageDigest.getInstance(type), value);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String hash(MessageDigest digest, String src) {
		return toHexString(digest.digest(src.getBytes()));
	}

	private static String toHexString(byte[] bytes) {
		char[] values = new char[bytes.length * 2];
		int i = 0;
		for (byte b : bytes) {
			values[i++] = LETTER.toCharArray()[((b & 0xF0) >>> 4)];
			values[i++] = LETTER.toCharArray()[b & 0xF];
		}
		return String.valueOf(values);
	}

	public static Random getInstance() {
		if (instance == null) {
			instance = new Random();
			if (!instance.init()) {
				instance = null;
			}
		}
		return instance;
	}

	private boolean init() {
		try {
			KeyGenerator keygen = KeyGenerator.getInstance(DES);
			SecureRandom random = new SecureRandom();
			keygen.init(random);
			key = keygen.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return key != null;
	}

	public String encrypt(String data) throws Exception {
		if (null == data)
			return null;
		Cipher cipher = Cipher.getInstance(DES);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		StringBuffer sb = new StringBuffer();
		int blockSize = cipher.getBlockSize();
		int outputSize = cipher.getOutputSize(blockSize);
		byte[] src = data.getBytes();
		byte[] outBytes = new byte[outputSize];
		int i = 0;
		try {
			for (; i <= src.length - blockSize; i = i + blockSize) {
				int outLength = cipher.update(src, i, blockSize, outBytes);
				sb.append(bytesToString(outBytes, outLength));
			}
			if (i == src.length)
				outBytes = cipher.doFinal();
			else {
				outBytes = cipher.doFinal(src, i, src.length - i);
			}
			sb.append(bytesToString(outBytes));
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String decrypt(String data) throws Exception {
		if (null == data)
			return null;
		Cipher cipher = Cipher.getInstance(DES);
		cipher.init(Cipher.DECRYPT_MODE, key);
		StringBuffer sb = new StringBuffer();
		int blockSize = cipher.getBlockSize();
		int outputSize = cipher.getOutputSize(blockSize);
		byte[] src = stringToBytes(data);
		byte[] outBytes = new byte[outputSize];
		int i = 0;
		try {
			for (; i <= src.length - blockSize; i = i + blockSize) {
				int outLength = cipher.update(src, i, blockSize, outBytes);
				sb.append(new String(outBytes, 0, outLength));
			}
			if (i == src.length)
				outBytes = cipher.doFinal();
			else {
				outBytes = cipher.doFinal(src, i, src.length - i);
			}
			sb.append(new String(outBytes));
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String bytesToString(byte[] bs) {
		if (bs == null || bs.length == 0)
			return "";
		return bytesToString(bs, bs.length);
	}

	private String bytesToString(byte[] bs, int len) {
		if (bs == null || bs.length == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append(String.format("%02X", bs[i]));
		}
		return sb.toString();
	}

	private byte[] stringToBytes(String str) {
		if (str == null || str.length() < 2 || str.length() % 2 != 0)
			return new byte[0];
		int len = str.length();
		byte[] bs = new byte[len / 2];
		for (int i = 0; i * 2 < len; i++) {
			bs[i] = (byte) (Integer.parseInt(str.substring(i * 2, i * 2 + 2),
					16) & 0xFF);
		}
		return bs;
	}

	public static String shortUrl(String url) {
		// 可以自定义生成 MD5 加密字符传前的混合 KEY
		String key = "test";
		// 要使用生成 URL 的字符
		String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h",
				"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
				"u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
				"6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
				"U", "V", "W", "X", "Y", "Z" };
		// 对传入网址进行 MD5 加密
		String hex = md5(key + url);

		String[] resUrl = new String[4];
		for (int i = 0; i < 4; i++) {
			// 把加密字符按照 8 位一组 16 进制与 0x3FFFFFFF 进行位与运算
			String sTempSubString = hex.substring(i * 8, i * 8 + 8);
			// 这里需要使用 long 型来转换，因为 Inteper .parseInt() 只能处理 31 位 , 首位为符号位 ,
			// 如果不用long ，则会越界
			long lHexLong = 0x3FFFFFFF & Long.parseLong(sTempSubString, 16);
			String outChars = "";
			for (int j = 0; j < 6; j++) {
				// 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
				long index = 0x0000003D & lHexLong;
				// 把取得的字符相加
				outChars += chars[(int) index];
				// 每次循环按位右移 5 位
				lHexLong = lHexLong >> 5;
			}
			// 把字符串存入对应索引的输出数组
			resUrl[i] = outChars;
		}
		return resUrl[new Random().nextInt(4)];
	}

}