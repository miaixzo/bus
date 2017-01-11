package com.ukettle.www.serialize;

/**
 * 功能描述：序列化、反序列化
 * 
 * @author Kimi Liu
 * @Date Mar 09, 2014
 * @Time 20:33:21
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public abstract class ObjectSerializer extends Serializer {

	/**
	 * 序列化一个对象
	 * 
	 * @param object
	 *            对象序列化。
	 * @return 反序列化的对象的String。
	 */
	public abstract String serialize(Object object);

	/**
	 * 反序列化一个对象
	 * 
	 * @param string
	 *            反序列化对象的字符串表示形式。
	 * @return an Object. 反序列化后对象Object
	 */
	public abstract Object deserialize(String string);
}