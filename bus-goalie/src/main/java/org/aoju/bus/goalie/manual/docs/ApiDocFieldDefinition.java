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
 ********************************************************************************/
package org.aoju.bus.goalie.manual.docs;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 文档参数字段信息
 *
 * @author Kimi Liu
 * @version 6.0.8
 * @since JDK 1.8++
 */
@Data
public class ApiDocFieldDefinition {

    private static ParamHtmlBuilder paramHtmlBuilder = new ParamHtmlBuilder();
    private static ParamHtmlBuilder paramHtmlPdfBuilder = new ParamHtmlPdfBuilder();
    private static ParamMarkdownHtmlBuilder paramMarkdownHtmlBuilder = new ParamMarkdownHtmlBuilder();
    private static ResultHtmlBuilder resultHtmlBuilder = new ResultHtmlBuilder();

    private String name;
    private String dataType;
    private String required;
    private String example;
    private String description;
    private Class<?> elementClass;

    private List<ApiDocFieldDefinition> elements = Collections.emptyList();

    private boolean rootData;

    public String getResultHtml() {
        return resultHtmlBuilder.buildHtml(this);
    }

    public String getParamHtml(String nameVersion) {
        return paramHtmlBuilder.buildHtml(this, nameVersion);
    }

    public String getParamMarkdownHtml() {
        return paramMarkdownHtmlBuilder.buildHtml(this);
    }

    public String getParamHtmlPdf(String nameVersion) {
        return paramHtmlPdfBuilder.buildHtml(this, nameVersion);
    }


    @Override
    public String toString() {
        return "ApiDocFieldDefinition{" +
                "name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", required='" + required + '\'' +
                ", example='" + example + '\'' +
                ", description='" + description + '\'' +
                ", elementClass=" + elementClass +
                ", elements=" + elements +
                ", rootData=" + rootData +
                '}';
    }

}
