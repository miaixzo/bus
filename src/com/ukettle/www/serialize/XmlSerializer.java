package com.ukettle.www.serialize;

import com.thoughtworks.xstream.XStream;

/**
 * 功能描述：序列化、反序列化
 * 
 * @author Kimi Liu
 * @Date Mar 10, 2014
 * @Time 00:21:09
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class XmlSerializer extends ObjectSerializer {

	public String serialize(Object object) {
		XStream xstream = new XStream();
		xstream.processAnnotations(object.getClass());
		return xstream.toXML(object);
	}

	public Object deserialize(String string) {
		XStream xstream = new XStream();
		return xstream.fromXML(string);
	}

	public String getContentType() {
		return TYPE_XML_UTF8;
	}

}