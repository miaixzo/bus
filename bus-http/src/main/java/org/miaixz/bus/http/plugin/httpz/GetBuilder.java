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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.Httpd;

import java.util.Map;

/**
 * GET参数构造器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GetBuilder extends RequestBuilder<GetBuilder> {

    public GetBuilder(Httpd httpd) {
        super(httpd);
    }

    @Override
    public RequestCall build() {
        if (null != formMap) {
            url = append(url, formMap);
        }
        return new GetRequest(url, tag, formMap, headerMap, id).build(httpd);
    }

    protected String append(String url, Map<String, String> formMap) {
        if (null == url || null == formMap || formMap.isEmpty()) {
            return url;
        }
        StringBuilder builder = new StringBuilder();
        formMap.forEach((k, v) -> {
            if (builder.length() == 0) {
                builder.append(Symbol.QUESTION_MARK);
            } else if (builder.length() > 0) {
                builder.append(Symbol.AND);
            }
            builder.append(k);
            builder.append(Symbol.EQUAL).append(v);
        });
        return url + builder.toString();
    }

}
