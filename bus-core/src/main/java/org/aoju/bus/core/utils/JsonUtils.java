/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * fastjson工具类
 *
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
public class JsonUtils {

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

    // 转换为数组
    public static Object[] toArray(String text) {
        return toArray(text, null);
    }

    public static <T> T toBean(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
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
     * @param text 文本内容
     * @return json对象
     */
    public static Object textToJson(String text) {
        return JSON.parse(text);
    }

    /**
     * json字符串转化为map
     *
     * @param <K>  反射对象
     * @param <V>  反射对象
     * @param text 文本内容
     * @return json map
     */
    public static <K, V> Map<K, V> stringToCollect(String text) {
        return (Map<K, V>) JSONObject.parseObject(text);
    }

    /**
     * 转换JSON字符串为对象
     *
     * @param text  文本内容
     * @param clazz 对象
     * @return 对象
     */
    public static Object convertJsonToObject(String text, Class<?> clazz) {
        return JSONObject.parseObject(text, clazz);
    }

    /**
     * 将map转化为string
     *
     * @param <K> 反射对象
     * @param <V> 反射对象
     * @param map 对象
     * @return 对象
     */
    public static <K, V> String collectToString(Map<K, V> map) {
        return JSONObject.toJSONString(map);
    }

    /**
     * JSON转换为List
     *
     * @param <T>   反射对象
     * @param text  json字符串
     * @param clazz 对象
     * @return the list
     */
    public static <T> List<T> convertJsonToList(String text, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        if (!text.equals("")) {

            if (text.contains("{") || text.contains("[")) {
                text = text;
            } else {
                // 转码
                try {
                    text = URLDecoder.decode(text, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    throw new InstrumentException(e);
                }
            }
            JSON alljson = (JSON) JSON.parse(text);
            List<JSON> alljsonlist = JSON.toJavaObject(alljson, List.class);
            for (int i = 0; i < alljsonlist.size(); i++) {
                JSON json = alljsonlist.get(i);
                try {
                    list.add(JSON.toJavaObject(json, (Class<T>) clazz.newInstance().getClass()));
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
     * @param object 对象
     * @return json字符
     */
    public static String toJson(Object object) {
        try {
            return JSON.toJSON(object).toString();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * object为可以转换为JSON对象的入参,例如:{
     * '':''}字符串,以及其他普通对象,不包含基本类型和封装类型[String除外但要符合JSON规则] 例如
     *
     * @param object 对象
     * @return json字符
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
