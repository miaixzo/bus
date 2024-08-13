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
package org.miaixz.bus.pay.magic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 实体公用方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    /**
     * 申请商户号的appid或商户号绑定的appid 企业号corpid即为此appid
     */
    private String appid;

    /**
     * 将建构的 builder 转为 Map
     *
     * @return 转化后的 Map
     */
    public Map<String, String> toMap() {
        String[] fieldNames = getNames(this);
        HashMap<String, String> map = new HashMap<>(fieldNames.length);
        for (String name : fieldNames) {
            String value = (String) getFieldValueByName(name, this);
            if (StringKit.isNotEmpty(value)) {
                map.put(name, value);
            }
        }
        return map;
    }

    /**
     * 获取属性名数组
     *
     * @param object 对象
     * @return 返回对象属性名数组
     */
    public String[] getNames(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        String[] names = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            names[i] = fields[i].getName();
        }
        return names;
    }

    /**
     * 根据属性名获取属性值
     *
     * @param field  属性名称
     * @param object 对象
     * @return 返回对应属性的值
     */
    public Object getFieldValueByName(String field, Object object) {
        try {
            String firstLetter = field.substring(0, 1).toUpperCase();
            String getter = new StringBuffer().append("get").append(firstLetter).append(field.substring(1)).toString();
            Method method = object.getClass().getMethod(getter);
            return method.invoke(object);
        } catch (Exception e) {
            return null;
        }
    }

}
