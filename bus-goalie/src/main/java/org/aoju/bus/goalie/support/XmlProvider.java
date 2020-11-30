/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.goalie.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.goalie.Provider;

import java.util.Map;
import java.util.Set;

/**
 * Xml序列化
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8++
 */
public class XmlProvider implements Provider {

    @Override
    public String serialize(Object obj) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            JSONObject jObj = JSON.parseObject(JSON.toJSONString(obj));
            json2Xml(jObj, buffer);
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Normal.EMPTY;
    }


    public static void json2Xml(JSONObject jObj, StringBuffer buffer) {
        Set<Map.Entry<String, Object>> se = jObj.entrySet();
        for (Map.Entry<String, Object> en : se) {
            switch (en.getValue().getClass().getName()) {
                case "com.alibaba.fastjson.JSONObject":
                    buffer.append("<").append(en.getKey()).append(">");
                    JSONObject jo = jObj.getJSONObject(en.getKey());
                    json2Xml(jo, buffer);
                    buffer.append("</").append(en.getKey()).append(">");
                    break;
                case "com.alibaba.fastjson.JSONArray":
                    JSONArray jsonArray = jObj.getJSONArray(en.getKey());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        buffer.append("<").append(en.getKey()).append(">");
                        JSONObject jsonobject = jsonArray.getJSONObject(i);
                        json2Xml(jsonobject, buffer);
                        buffer.append("</").append(en.getKey()).append(">");
                    }
                    break;
                case "java.lang.String":
                    buffer.append("<").append(en.getKey()).append(">").append(en.getValue());
                    buffer.append("</").append(en.getKey()).append(">");
                    break;
            }

        }
    }


}
