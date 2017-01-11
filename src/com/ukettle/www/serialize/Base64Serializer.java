package com.ukettle.www.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;

/**
 * Default base64 serializer/deserializer.
 * 
 */
/**
 * 功能描述：注解属性
 * 
 * @author Kimi Liu
 * @Date May 10, 2014
 * @Time 18:21:53
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class Base64Serializer extends ObjectSerializer {

	public String serialize(Object object) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			return new String(Base64.encodeBase64(bos.toByteArray()));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Object deserialize(String data) {
		if ((data == null) || (data.length() == 0)) {
			return null;
		}
		ObjectInputStream ois = null;
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(Base64.decodeBase64(data.getBytes()));
			ois = new ObjectInputStream(bis);
			return ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getContentType() {
		return TYPE_STREAM_UTF8;
	}

}