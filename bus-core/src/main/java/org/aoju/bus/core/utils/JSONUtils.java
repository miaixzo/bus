package org.aoju.bus.core.utils;

import org.aoju.bus.core.lang.exception.InstrumentException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * fastjson工具类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class JSONUtils {

    private static final SerializeConfig config;
    private static final SerializerFeature[] features = {SerializerFeature.WriteMapNullValue, // 输出空置字段
            SerializerFeature.WriteNullListAsEmpty, // list字段如果为null，输出为[]，而不是null
            SerializerFeature.WriteNullNumberAsZero, // 数值字段如果为null，输出为0，而不是null
            SerializerFeature.WriteNullBooleanAsFalse, // Boolean字段如果为null，输出为false，而不是null
            SerializerFeature.WriteNullStringAsEmpty // 字符类型字段如果为null，输出为""，而不是null
    };

    static {
        config = new SerializeConfig();
        config.put(java.util.Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
        config.put(java.sql.Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
    }

    public static String convertObjectToJSON(Object object) {
        return JSON.toJSONString(object, config, features);
    }

    public static String toJSONNoFeatures(Object object) {
        return JSON.toJSONString(object, config);
    }


    public static Object toBean(String text) {
        return JSON.parse(text);
    }

    public static <T> T toBean(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    // 转换为数组
    public static <T> Object[] toArray(String text) {
        return toArray(text, null);
    }

    // 转换为数组
    public static <T> Object[] toArray(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz).toArray();
    }

    // 转换为List
    public static <T> List<T> toList(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }

    /**
     * 将string转化为序列化的json字符串
     *
     * @param text
     * @return
     */
    public static Object textToJson(String text) {
        Object objectJson = JSON.parse(text);
        return objectJson;
    }

    /**
     * json字符串转化为map
     *
     * @param s
     * @return
     */
    public static <K, V> Map<K, V> stringToCollect(String s) {
        Map<K, V> m = (Map<K, V>) JSONObject.parseObject(s);
        return m;
    }

    /**
     * 转换JSON字符串为对象
     *
     * @param jsonData
     * @param clazz
     * @return
     */
    public static Object convertJsonToObject(String jsonData, Class<?> clazz) {
        return JSONObject.parseObject(jsonData, clazz);
    }

    public static Object convertJSONToObject(String content, Class<?> clazz) {
        return JSONObject.parseObject(content, clazz);
    }

    /**
     * 将map转化为string
     *
     * @param m
     * @return
     */
    public static <K, V> String collectToString(Map<K, V> m) {
        return JSONObject.toJSONString(m);
    }


    /**
     * JSON转换为普通对象
     *
     * @param st
     * @param t
     * @return
     */
    public static <T> T toObject(String st, Class<T> t) {
        if (!st.equals("")) {
            if (st.contains("{") || st.contains("[")) {
                return JSON.parseObject(st, t);
            } else {
                // 转码
                return JSON.parseObject(URLDecoder.decode(st), t);
            }
        }
        return null;
    }

    /**
     * JSON转换为List
     *
     * @param st
     * @param t
     * @return
     */
    public static <T> List<T> convertJSONlist(String st, Class<T> t) {
        List<T> list = new ArrayList<T>();
        if (!st.equals("")) {

            if (st.contains("{") || st.contains("[")) {
                st = st;
            } else {
                // 转码
                try {
                    st = URLDecoder.decode(st, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    throw new InstrumentException(e);
                }
            }
            JSON alljson = (JSON) JSON.parse(st);
            List<JSON> alljsonlist = JSON.toJavaObject(alljson, List.class);
            for (int i = 0; i < alljsonlist.size(); i++) {
                JSON json = alljsonlist.get(i);
                try {
                    list.add(JSON.toJavaObject(json, (Class<T>) t.newInstance().getClass()));
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new InstrumentException(e);
                }
            }
        }
        return list;
    }

    /**
     * object为可以转换为JSON对象的入参,例如:{'':''}字符串,以及其他普通对象,包含基本类型和封装类型,
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        String json = "";
        try {
            json = JSON.toJSON(object).toString();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
        return json;
    }

    /**
     * object为可以转换为JSON对象的入参,例如:{
     * '':''}字符串,以及其他普通对象,不包含基本类型和封装类型[String除外但要符合JSON规则] 例如
     *
     * @param object
     * @return
     * @author 汪旭辉
     * @date 2016年5月6日
     */
    public static JSON toJsonBean(Object object) {
        try {
            if (object == null) {
                return null;
            }
            // 对象转换
            return (JSON) JSON.toJSON(object);
        } catch (ClassCastException e) {
            // 类型失败后,转换为string类型
            return JSON.parseObject("" + object.toString());
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

}
