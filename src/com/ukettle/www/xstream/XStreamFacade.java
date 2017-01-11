package com.ukettle.www.xstream;

import java.io.Writer;
import java.lang.reflect.Field;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.ukettle.www.toolkit.Constant;
import com.ukettle.www.xstream.annotations.XStream2Field;
import com.ukettle.www.xstream.annotations.XStream2Type;

/**
 * 功能描述：创建文件输出信息
 * 
 * @author Kimi Liu
 * @Date May 11, 2014
 * @Time 15:33:10
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public final class XStreamFacade {

	private static XStream xstream;
	public static final String JAXP_DOM_XML = "JAXP DOM";
	public static final String XPP3_XML_PARSER = "XPP3";
	public static final String STAX_JSON_PARSER = "Jettison StAX";
	public static final String WRITER_JSON_PARSER = "Only Writer JSON";

	/** CDATA 前缀 */
	private static final String PREFIX_CDATA = "<![CDATA[";
	/** CDATA 后缀 */
	private static final String SUFFIX_CDATA = "]]>";

	public synchronized static XStream get(String type) {
		if (JAXP_DOM_XML.equals(type)) {
			xstream = createXMLXStream();
			xstream.autodetectAnnotations(true);
		} else if (XPP3_XML_PARSER.equals(type)) {
			xstream = new XStream(new XppDriver());
			xstream.autodetectAnnotations(true);
		} else if (STAX_JSON_PARSER.equals(type)) {
			xstream = new XStream(new JettisonMappedXmlDriver());
			xstream.setMode(XStream.NO_REFERENCES);
		} else if (WRITER_JSON_PARSER.equals(type)) {
			xstream = new XStream(new JsonHierarchicalStreamDriver());
			xstream.setMode(XStream.NO_REFERENCES);
		} else {
			xstream = createXMLXStream();
			xstream.autodetectAnnotations(true);
		}
		return xstream;
	}

	/**
	 * 功能描述：创建xml转换器
	 * 
	 * @return xstream 转换信息
	 */
	@SuppressWarnings("all")
	public static XStream createXMLXStream() {
		return new XStream(new DomDriver(Constant.DEFAULT_ENCODING) {
			@Override
			public HierarchicalStreamWriter createWriter(Writer out) {
				return new PrettyPrintWriter(out) {
					boolean cdata = false;
					Class<?> targetClass = null;

					@Override
					public void startNode(String name, Class clazz) {
						super.startNode(name, clazz);
						// 业务处理，对于用XStream2Field标记的Field，需要加上CDATA标签
						XStream2Type xStream2Type = (XStream2Type) clazz
								.getAnnotation(XStream2Type.class);
						if (null == xStream2Type) {
							// if(!name.equals("xml"))
							cdata = needCDATA(targetClass, name);
						} else {
							targetClass = clazz;
						}
					}

					@Override
					protected void writeText(QuickWriter writer, String text) {
						if (cdata) {
							writer.write(addCDATA(text));
						} else {
							writer.write(text);
						}
					}
				};
			}
		});
	}

	/**
	 * 功能描述：转换字符信息
	 * 
	 * @return string 转换后的字符
	 */
	private static String addCDATA(String text) {
		return PREFIX_CDATA + text + SUFFIX_CDATA;
	}

	/**
	 * 功能描述：从所有的类扫描XStreamCDATA标签
	 * 
	 * @param targetClass
	 *            属性类型
	 * @param fieldAlias
	 *            属性名称
	 * @return boolean 是否
	 */
	private static boolean needCDATA(Class<?> targetClass, String fieldAlias) {
		boolean cdata = false;
		// first, scan self
		cdata = existsCDATA(targetClass, fieldAlias);
		if (cdata)
			return cdata;
		// if cdata is false, scan supperClass until java.lang.Object
		Class<?> superClass = targetClass.getSuperclass();
		while (!superClass.equals(Object.class)) {
			cdata = existsCDATA(superClass, fieldAlias);
			if (cdata)
				return cdata;
			superClass = superClass.getClass().getSuperclass();
		}
		return false;
	}

	/**
	 * 功能描述：检查字段是否存在XStreamCDATA标签
	 * 
	 * @param clazz
	 *            属性类型
	 * @param fieldAlias
	 *            XStream 属性名称
	 * @return boolean 是否
	 */
	private static boolean existsCDATA(Class<?> clazz, String fieldAlias) {
		// scan fields
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			// 1. exists XStreamCDATA
			if (null != field.getAnnotation(XStream2Field.class)) {
				XStreamAlias xStreamAlias = field
						.getAnnotation(XStreamAlias.class);
				// 2. exists XStreamAlias
				if (null != xStreamAlias) {
					if (fieldAlias.equals(xStreamAlias.value()))// matched
						return true;
				} else {// not exists XStreamAlias
					if (fieldAlias.equals(field.getName()))
						return true;
				}
			}
		}
		return false;
	}

}