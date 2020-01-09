/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.builtin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

import java.util.List;

/**
 * 接口内容
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
@Data
public class ApiDocItem implements Orderable {

    private String name;
    private String version;
    private String description;
    private String remark;
    private int order;

    private List<ApiDocFieldDefinition> paramDefinitions;
    private List<ApiDocFieldDefinition> resultDefinitions;
    /**
     * 单值返回
     */
    private ApiDocReturnDefinition apiDocReturnDefinition;

    private Object paramData;
    private Object resultData;

    private boolean customWrapper;

    public String getNameVersion() {
        return this.name + this.version;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String getName() {
        return this.getNameVersion();
    }

    public String getParamData() {
        return JSON.toJSONString(paramData, SerializerFeature.PrettyFormat);
    }

    public String getResultData() {
        return JSON.toJSONString(resultData, SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullNumberAsZero
        );
    }

    public Object fatchResultData() {
        return this.resultData;
    }

}
