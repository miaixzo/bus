/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.toolkit.BeanKit;
import org.miaixz.bus.http.Httpd;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求参数构造器
 *
 * @author Kimi Liu
 * @since Java 17+
 */

public abstract class RequestBuilder<T extends RequestBuilder> {

    protected Httpd httpd;
    protected String url;
    protected Object tag;
    protected Map<String, String> headers;
    protected Map<String, String> params;
    protected Map<String, String> encodedParams;
    protected String id;

    public RequestBuilder(Httpd httpd) {
        this.httpd = httpd;
        headers = new LinkedHashMap<>();
        params = new LinkedHashMap<>();
        encodedParams = new LinkedHashMap<>();
    }

    public T id(String id) {
        this.id = id;
        return (T) this;
    }

    public T url(String url) {
        this.url = url;
        return (T) this;
    }

    public T tag(Object tag) {
        this.tag = tag;
        return (T) this;
    }

    public T headers(Map<String, String> headers) {
        this.headers = headers;
        return (T) this;
    }

    public T addHeaders(Map<String, String> headers) {
        if (null != headers) {
            headers.forEach((k, v) -> this.headers.put(k, v));
        }
        return (T) this;
    }

    public T addHeader(String key, String val) {
        headers.put(key, val);
        return (T) this;
    }

    public T params(Map<String, String> params) {
        this.params = params;
        return (T) this;
    }

    public T addParams(String key, String val) {
        this.params.put(key, val);
        return (T) this;
    }

    public T addParams(Map<String, String> paramMap) {
        if (null == paramMap) {
            return (T) this;
        }
        paramMap.forEach((k, v) -> params.put(k, v));
        return (T) this;
    }

    public T addParams(Object object) {
        if (null != object) {
            Map<String, Object> map = BeanKit.beanToMap(object);
            map.forEach((key, val) -> addParams(key, (String) val));
        }
        return (T) this;
    }


    public T encodedParams(Map<String, String> params) {
        this.encodedParams = params;
        return (T) this;
    }

    public T addEncodedParams(String key, String val) {
        this.encodedParams.put(key, val);
        return (T) this;
    }

    public abstract RequestCall build();

}
