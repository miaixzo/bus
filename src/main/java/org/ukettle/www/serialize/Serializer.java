package org.ukettle.www.serialize;

/**
 * 功能描述：信息序列化
 * 
 * @author Kimi Liu
 * @Date Mar 10, 2014
 * @Time 00:21:09
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public abstract class Serializer {

	public static String TYPE_TEXT_UTF8 = "text/plain; charset=UTF-8";
	public static String TYPE_JSON_UTF8 = "application/json; charset=UTF-8";
	public static String TYPE_XML_UTF8 = "text/xml; charset=UTF-8";
	public static String TYPE_BINARY_UTF8 = "application/octet-stream; charset=UTF-8";
	public static String TYPE_STREAM_UTF8 = "application/octet-stream; charset=UTF-8";

	/**
	 * 功能描述：获取输出类型
	 * 
	 * @return String 输出类型
	 */
	public abstract String getContentType();
}