/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.extra.json;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * json工具类,通过SPI自动识别
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JsonKit {

    /**
     * 获得全局单例的json映射器
     *
     * @return 全局单例的json映射器
     */
    public static JsonProvider getProvider() {
        return JsonFactory.get();
    }

    /**
     * 解析对象为Json字符串
     *
     * @param object 要转换的对象
     * @return 返回对象的json字符串
     */
    public static String toJsonString(Object object) {
        return getProvider().toJsonString(object);
    }

    /**
     * 解析对象为Json字符串
     *
     * @param object 要转换的对象
     * @param format 日期格式，如"yyyy年MM月dd日 HH时mm分ss秒"
     * @return 返回对象的json字符串
     */
    public static String toJsonString(Object object, String format) {
        return getProvider().toJsonString(object, format);
    }

    /**
     * 解析json字符串到指定类型的对象
     *
     * @param json  要解析的json字符串
     * @param clazz 类对象class
     * @param <T>   泛型参数类型
     * @return 返回解析后的对象
     */
    public static <T> T toPojo(String json, Class<T> clazz) {
        return getProvider().toPojo(json, clazz);
    }

    /**
     * 从Map转换到对象
     *
     * @param map   Map对象
     * @param clazz 与Map可兼容的对象类型
     * @param <T>   泛型参数类型
     * @return 返回Map转换得到的对象
     */
    public static <T> T toPojo(Map map, Class<T> clazz) {
        return getProvider().toPojo(map, clazz);
    }

    /**
     * 解析json字符串到List
     *
     * @param json 要解析的json字符串
     * @return 返回List
     */
    public static List toList(String json) {
        return getProvider().toList(json);
    }

    /**
     * 按指定的Type解析json字符串到List
     *
     * @param json 要解析的json字符串
     * @param type {@link Type}
     * @param <T>  泛型参数类型
     * @return 返回List
     */
    public static <T> List<T> toList(String json, final Type type) {
        return getProvider().toList(json, type);
    }

    /**
     * 按指定的Type解析json字符串到List
     *
     * @param json  要解析的json字符串
     * @param clazz 类对象class
     * @param <T>   泛型参数类型
     * @return 返回List
     */
    public static <T> List<T> toList(String json, final Class<T> clazz) {
        return getProvider().toList(json, clazz);
    }

    /**
     * 解析json字符串到Map
     *
     * @param json 要解析的json字符串
     * @return 返回Map
     */
    public static Map toMap(String json) {
        return getProvider().toMap(json);
    }

    /**
     * 转换对象到Map
     *
     * @param object 与Map可兼容的对象
     * @return 返回Map对象
     */
    public static Map toMap(Object object) {
        return getProvider().toMap(object);
    }

    /**
     * 获取json字符串指定属性值
     *
     * @param json  要解析的json字符串
     * @param field 属性名称
     * @param <T>   泛型参数类型
     * @return 返回解析后的属性值
     */
    public static <T> T getValue(String json, String field) {
        return getProvider().getValue(json, field);
    }

    /**
     * 判断是否为标准json
     *
     * @param json 字符串
     * @return the true/false
     */
    public static boolean isJson(String json) {
        return getProvider().isJson(json);
    }

}
