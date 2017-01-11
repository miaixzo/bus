package com.ukettle.www.toolkit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.ukettle.www.exception.uKettleException;

/**
 * <p>
 * 封装各种格式的编码解码工具类. 1.Commons-Codec的 hex/base64 编码 2.自制的base62 编码
 * 3.Commons-Lang的xml/html escape 4.JDK提供的URLEncoder
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 15, 2014
 * @Time 10:22:21
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class Encode {

	private static String DEFAULT_URL_ENCODING = Constant.DEFAULT_ENCODING;
	private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
			.toCharArray();

	/**
	 * Hex编码.
	 */
	public static String encodeHex(byte[] input) {
		return Hex.encodeHexString(input);
	}

	/**
	 * Hex解码.
	 */
	public static byte[] decodeHex(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw uKettleException.unchecked(e);
		}
	}

	/**
	 * Base64编码.
	 */
	public static String encodeBase64(byte[] input) {
		return Base64.encodeBase64String(input);
	}

	/**
	 * Base64编码, URL安全(将Base64中的URL非法字符'+'和'/'转为'-'和'_', 见RFC3548).
	 */
	public static String encodeUrlSafeBase64(byte[] input) {
		return Base64.encodeBase64URLSafeString(input);
	}

	/**
	 * Base64解码.
	 */
	public static byte[] decodeBase64(String input) {
		return Base64.decodeBase64(input);
	}

	/**
	 * Base62编码。
	 */
	public static String encodeBase62(byte[] input) {
		char[] chars = new char[input.length];
		for (int i = 0; i < input.length; i++) {
			chars[i] = BASE62[((input[i] & 0xFF) % BASE62.length)];
		}
		return new String(chars);
	}

	/**
	 * URL 编码, Encode默认为UTF-8.
	 */
	public static String urlEncode(String url, String encoding) {
		try {
			if (null != encoding && !"".equals(encoding)) {
				DEFAULT_URL_ENCODING = encoding;
			}
			return URLEncoder.encode(url, DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw uKettleException.unchecked(e);
		}
	}

	/**
	 * URL 解码, Encode默认为UTF-8.
	 */
	public static String urlDecode(String url, String encoding) {
		try {
			if (null != encoding && !"".equals(encoding)) {
				DEFAULT_URL_ENCODING = encoding;
			}
			return URLDecoder.decode(url, DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw uKettleException.unchecked(e);
		}
	}

}