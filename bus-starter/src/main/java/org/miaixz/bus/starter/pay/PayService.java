/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.pay;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Provider;
import org.miaixz.bus.pay.Registrar;
import org.miaixz.bus.pay.magic.ErrorCode;
import org.miaixz.bus.pay.metric.alipay.AliPayProvider;
import org.miaixz.bus.pay.metric.jdpay.JdPayProvider;
import org.miaixz.bus.pay.metric.paypal.PaypalProvider;
import org.miaixz.bus.pay.metric.tenpay.TenpayProvider;
import org.miaixz.bus.pay.metric.unionpay.UnionPayProvider;
import org.miaixz.bus.pay.metric.wechat.WechatPayProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Integrated payment service. This service manages different payment providers and their configurations. It allows for
 * dynamic registration and retrieval of payment providers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PayService {

    /**
     * A cache to store payment provider contexts, keyed by their registry type.
     */
    private static final Map<Registrar, Context> CACHE = new ConcurrentHashMap<>();

    /**
     * Payment configuration properties.
     */
    public PayProperties properties;

    /**
     * Cache instance for handling caching operations.
     */
    public CacheX cache;

    /**
     * Complex payment parameters.
     */
    public Complex complex;

    /**
     * Constructs a new PayService with the given properties.
     *
     * @param properties The payment configuration properties.
     */
    public PayService(PayProperties properties) {
        this.properties = properties;
    }

    /**
     * Constructs a new PayService with the given properties and complex parameters.
     *
     * @param properties The payment configuration properties.
     * @param complex    The complex payment parameters.
     */
    public PayService(PayProperties properties, Complex complex) {
        this.properties = properties;
        this.complex = complex;
    }

    /**
     * Constructs a new PayService with the given properties, complex parameters, and cache.
     *
     * @param properties The payment configuration properties.
     * @param complex    The complex payment parameters.
     * @param cache      The cache instance.
     */
    public PayService(PayProperties properties, Complex complex, CacheX cache) {
        this.properties = properties;
        this.complex = complex;
        this.cache = cache;
    }

    /**
     * Registers a new payment provider context.
     *
     * @param registry The registry type of the payment provider.
     * @param context  The context object for the payment provider.
     * @throws InternalException if a provider with the same registry name is already registered.
     */
    public static void register(Registrar registry, Context context) {
        if (CACHE.containsKey(registry)) {
            throw new InternalException("A component with the same name is already registered: " + registry.name());
        }
        CACHE.putIfAbsent(registry, context);
    }

    /**
     * Retrieves a payment provider instance based on the registry type. It first checks the local cache, then falls
     * back to the properties.
     *
     * @param registry The {@link Registrar} type of the required provider.
     * @return The {@link Provider} instance.
     * @throws InternalException if the requested provider is not supported or cannot be found.
     */
    public Provider require(Registrar registry) {
        Context context = CACHE.get(registry);
        if (ObjectKit.isEmpty(context)) {
            context = this.properties.getType().get(registry);
        }
        switch (registry) {
            case ALIPAY:
                return new AliPayProvider(context, complex, cache);

            case JDPAY:
                return new JdPayProvider(context, complex, cache);

            case PAYPAL:
                return new PaypalProvider(context, complex, cache);

            case TENPAY:
                return new TenpayProvider(context, complex, cache);

            case UNIONPAY:
                return new UnionPayProvider(context, complex, cache);

            case WECHAT:
                return new WechatPayProvider(context, complex, cache);

            default:
                throw new InternalException(ErrorCode._100803.getValue());
        }
    }

}
