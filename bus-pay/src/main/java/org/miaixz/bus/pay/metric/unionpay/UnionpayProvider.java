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
package org.miaixz.bus.pay.metric.unionpay;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.metric.AbstractProvider;
import org.miaixz.bus.pay.metric.unionpay.api.UnionPayApi;

import java.util.Map;

/**
 * 云闪付接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UnionPayProvider extends AbstractProvider<Material, Context> {

    public UnionPayProvider(Context context) {
        this(context, null);
    }

    public UnionPayProvider(Context context, Complex complex) {
        this(context, complex, null);
    }

    public UnionPayProvider(Context context, Complex complex, ExtendCache cache) {
        super(context, complex, cache);
    }

    public static String execution(String url, Map<String, String> params) {
        return post(url, XmlKit.mapToXmlString(params));
    }

    /**
     * 获取接口请求的 URL
     *
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * 获取接口请求的 URL
     *
     * @param complex {@link UnionPayApi} 支付 API 接口枚举
     * @return {@link String} 返回完整的接口请求URL
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.UNIONPAY.sandbox() : Registry.UNIONPAY.service())
                .concat(complex.method());
    }

}
