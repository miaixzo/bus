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


import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * Parsing The Configuration File
 *
 * @author Kimi Liu
 * @version 5.0.3
 * @since JDK 1.8+
 */
public final class PropertiesUtils {

    private static Map<String, String> ctxPropertiesMap = new HashMap<>();

    public static Map<String, String> getProperties() {
        return ctxPropertiesMap;
    }

    public static String getString(String key) {
        try {
            return ctxPropertiesMap.get(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    public static String getString(String key, String defaultValue) {
        try {
            String value = ctxPropertiesMap.get(key);
            if (ObjectUtils.isEmpty(value)) {
                return defaultValue;
            }
            return value;
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    /**
     * 根据key获取值
     *
     * @param key 键
     * @return the int
     */
    public static int getInt(String key) {
        return Integer.parseInt(ctxPropertiesMap.get(key));
    }

    /**
     * 根据key获取值
     *
     * @param key          键
     * @param defaultValue 值
     * @return the int
     */
    public static int getInt(String key, int defaultValue) {
        String value = ctxPropertiesMap.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    /**
     * 根据key获取值
     *
     * @param key          键
     * @param defaultValue 值
     * @return the boolean
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = ctxPropertiesMap.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return new Boolean(value);
    }

    /**
     * @param className   当前类的全类名
     * @param path        要获取的properties配置文件路径
     * @param propertyKey 配置文件中的key
     * @return the string
     */
    public static String getString(String className, String path, String propertyKey) {
        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InstrumentException(e);
        }
        InputStream in = clazz.getResourceAsStream(path);
        Properties prop = new Properties();
        try {
            prop.load(in);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return prop.getProperty(propertyKey);
    }

    public static Reader getReader(String classpath) {
        InputStream is = PropertiesUtils.class.getResourceAsStream(classpath);
        return new InputStreamReader(is, StandardCharsets.UTF_8);
    }

}
