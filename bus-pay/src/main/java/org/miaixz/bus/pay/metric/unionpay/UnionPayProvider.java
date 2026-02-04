/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay.metric.unionpay;

import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.Voucher;
import org.miaixz.bus.pay.metric.AbstractProvider;

/**
 * UnionPay Cloud QuickPass provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UnionPayProvider extends AbstractProvider<Voucher, Context> {

    /**
     * Constructs a new UnionPayProvider.
     *
     * @param context The context.
     */
    public UnionPayProvider(Context context) {
        this(context, null);
    }

    /**
     * Constructs a new UnionPayProvider.
     *
     * @param context The context.
     * @param complex The complex object.
     */
    public UnionPayProvider(Context context, Complex complex) {
        this(context, complex, null);
    }

    /**
     * Constructs a new UnionPayProvider.
     *
     * @param context The context.
     * @param complex The complex object.
     * @param cache   The cache.
     */
    public UnionPayProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
    }

    /**
     * Executes a POST request with the given URL and parameters.
     *
     * @param url    The URL to post to.
     * @param params The parameters to post.
     * @return The response from the server.
     */
    public static String execution(String url, Map<String, String> params) {
        return post(url, XmlKit.mapToXmlString(params));
    }

    /**
     * Gets the complete URL for the API request.
     *
     * @return The complete URL.
     */
    public String getUrl() {
        return getUrl(this.complex);
    }

    /**
     * Gets the complete URL for the API request.
     *
     * @param complex The payment API interface enumeration.
     * @return The complete URL.
     */
    public String getUrl(Complex complex) {
        return (complex.isSandbox() ? Registry.UNIONPAY.sandbox() : Registry.UNIONPAY.service())
                .concat(complex.method());
    }

}
