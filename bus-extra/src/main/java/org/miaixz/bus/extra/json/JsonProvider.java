/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.json;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumMap;

/**
 * JSON服务提供者
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface JsonProvider extends Provider {

    /**
     * 解析对象为Json字符串
     *
     * @param object 要转换的对象
     * @return 返回对象的json字符串
     */
    String toJsonString(Object object);

    /**
     * 解析对象为Json字符串
     *
     * @param object 要转换的对象
     * @param format 日期格式，如"yyyy年MM月dd日 HH时mm分ss秒"
     * @return 返回对象的json字符串
     */
    String toJsonString(Object object, String format);

    /**
     * 解析json字符串到指定类型的对象
     *
     * @param json  要解析的json字符串
     * @param clazz 类对象class
     * @param <T>   泛型参数类型
     * @return 返回解析后的对象
     */
    <T> T toPojo(String json, Class<T> clazz);

    /**
     * 从Map转换到对象
     *
     * @param map   Map对象
     * @param clazz 与Map可兼容的对象类型
     * @param <T>   泛型参数类型
     * @return 返回Map转换得到的对象
     */
    <T> T toPojo(Map map, Class<T> clazz);

    /**
     * 解析json字符串到List
     *
     * @param json 要解析的json字符串
     * @param <T>  泛型参数类型
     * @return 返回List
     */
    <T> List<T> toList(String json);

    /**
     * 按指定的Type解析json字符串到List
     *
     * @param json  要解析的json字符串
     * @param clazz 类对象class
     * @param <T>   泛型参数类型
     * @return 返回List
     */
    <T> List<T> toList(String json, Class<T> clazz);

    /**
     * 按指定的Type解析json字符串到List
     *
     * @param json 要解析的json字符串
     * @param type {@link Type}
     * @param <T>  泛型参数类型
     * @return 返回List
     */
    <T> List<T> toList(String json, final Type type);

    /**
     * 解析json字符串到Map
     *
     * @param json 要解析的json字符串
     * @param <K>  键类型
     * @param <V>  值类型
     * @return 返回Map
     */
    <K, V> Map<K, V> toMap(String json);

    /**
     * 转换对象到Map
     *
     * @param object 与Map可兼容的对象
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 返回Map对象
     */
    <K, V> Map<K, V> toMap(Object object);

    /**
     * 获取json字符串指定属性值
     *
     * @param json  要解析的json字符串
     * @param field 属性名称
     * @param <T>   泛型参数类型
     * @return 返回解析后的属性值
     */
    <T> T getValue(String json, String field);

    /**
     * 判断是否为标准json
     *
     * @param json 字符串
     * @return the true/false
     */
    boolean isJson(String json);

    @Override
    default Object type() {
        return EnumMap.Povider.JSON;
    }

}
