package org.ukettle.www.serialize;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class JsonSerializer extends ObjectSerializer {

	public String serialize(Object object) {
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.processAnnotations(object.getClass());
		return xstream.toXML(object);
	}

	public Object deserialize(String json) {
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		return xstream.fromXML(json);
	}

	public String getContentType() {
		return TYPE_JSON_UTF8;
	}

}