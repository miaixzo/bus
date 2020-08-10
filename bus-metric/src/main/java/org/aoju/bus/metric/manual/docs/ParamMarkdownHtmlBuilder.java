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
package org.aoju.bus.metric.manual.docs;

import org.aoju.bus.core.toolkit.CollKit;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8++
 */
public class ParamMarkdownHtmlBuilder {

    private static final String START_TD = "<td>";
    private static final String END_TD = "</td>";
    private static final String START_TR = "<tr>";
    private static final String END_TR = "</tr>";
    private static final String TRUE = "true";


    public String buildHtml(ApiDocFieldDefinition definition) {
        StringBuilder html = new StringBuilder();
        html.append(START_TR)
                .append(wrapTD(definition.getName()))
                .append("<td class=\"param-type\">" + definition.getDataType() + END_TD)
                .append(wrapTD(this.getRequireHtml(definition)))
                .append(wrapTD(buildExample(definition)))
                .append(wrapTD(definition.getDescription()));
        html.append(END_TR);

        return html.toString();
    }

    private String wrapTD(String content) {
        return START_TD + content + END_TD;
    }

    protected String buildExample(ApiDocFieldDefinition definition) {
        StringBuilder html = new StringBuilder();
        if (CollKit.isNotEmpty(definition.getElements())) {
            html.append("<table parentname=\"" + definition.getName() + "\">")
                    .append(START_TR)
                    .append("<th>名称</th>")
                    .append("<th>类型</th>")
                    .append("<th>是否必须</th>")
                    .append("<th>示例值</th>")
                    .append("<th>描述</th>")
                    .append(END_TR);

            List<ApiDocFieldDefinition> els = definition.getElements();
            for (ApiDocFieldDefinition apiDocFieldDefinition : els) {
                html.append(START_TR)
                        .append(wrapTD(apiDocFieldDefinition.getName()))
                        .append("<td class=\"param-type\">" + apiDocFieldDefinition.getDataType() + END_TD)
                        .append(wrapTD(getRequireHtml(apiDocFieldDefinition)))
                        .append(wrapTD(buildExample(apiDocFieldDefinition)))
                        .append(wrapTD(apiDocFieldDefinition.getDescription()))
                        .append(END_TR);
            }
            html.append("</table>");
        } else {
            html.append(buildExampleValue(definition));
        }
        return html.toString();
    }

    private String getRequireHtml(ApiDocFieldDefinition definition) {
        if (TRUE.equals(definition.getRequired())) {
            return "<strong>是</strong>";
        } else {
            return "否";
        }
    }

    protected String buildExampleValue(ApiDocFieldDefinition definition) {
        return definition.getExample();
    }

}
