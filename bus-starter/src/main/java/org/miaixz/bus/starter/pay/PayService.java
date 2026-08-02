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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.pay.Complex;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Provider;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.magic.ErrorCode;
import org.miaixz.bus.pay.nimble.alipay.AliPayProvider;
import org.miaixz.bus.pay.nimble.jdpay.JdPayProvider;
import org.miaixz.bus.pay.nimble.paypal.PaypalProvider;
import org.miaixz.bus.pay.nimble.tenpay.TenpayProvider;
import org.miaixz.bus.pay.nimble.unionpay.UnionPayProvider;
import org.miaixz.bus.pay.nimble.wechat.WechatPayProvider;

/**
 * Integrated payment service. This service manages different payment providers and their configurations. It allows for
 * dynamic registration and retrieval of payment providers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PayService implements AutoCloseable {

    /**
     * A cache to store payment provider contexts, keyed by their registry type.
     */
    private final Map<Registry, Context> contexts = new ConcurrentHashMap<>();

    /**
     * Payment clients created for this application context.
     */
    private final Map<Registry, Provider<?>> clients = new ConcurrentHashMap<>();

    /**
     * Payment configuration properties.
     */
    private final PayProperties properties;

    /**
     * Cache instance for handling caching operations.
     */
    private final CacheX<String, Object> cache;

    /**
     * Complex payment parameters.
     */
    private final Complex complex;

    /**
     * Constructs a new PayService with the given properties.
     *
     * @param properties The payment configuration properties.
     * @param complex    whether complex expressions are enabled
     * @param cache      cache settings
     */
    public PayService(PayProperties properties, Complex complex, CacheX<String, Object> cache) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.complex = Objects.requireNonNull(complex, "complex");
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    /**
     * Registers a new payment provider context.
     *
     * @param registry The registry type of the payment provider.
     * @param context  The context object for the payment provider.
     * @throws InternalException if a provider with the same registry name is already registered.
     */
    public void register(Registry registry, Context context) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(context, "context");
        if (this.contexts.putIfAbsent(registry, context) != null) {
            throw new InternalException("A component with the same name is already registered: " + registry.name());
        }
    }

    /**
     * Retrieves a payment provider instance based on the registry type. It first checks the local cache, then falls
     * back to the properties.
     *
     * @param registry The {@link Registry} type of the required provider.
     * @return The {@link Provider} instance.
     * @throws InternalException if the requested provider is not supported or cannot be found.
     */
    public Provider<?> require(Registry registry) {
        Objects.requireNonNull(registry, "registry");
        return this.clients.computeIfAbsent(registry, this::createProvider);
    }

    /**
     * Resolves the configured payment provider from the injected registry.
     *
     * @param registry Provider registry used for lookup
     * @return the payment Provider selected for the requested channel
     */
    private Provider<?> createProvider(Registry registry) {
        Context context = this.contexts.get(registry);
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

    /**
     * Closes supported clients and clears all payment state owned by this application context.
     */
    @Override
    public void close() {
        RuntimeException failure = null;
        for (Provider<?> client : this.clients.values()) {
            if (client instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception exception) {
                    if (failure == null) {
                        failure = new IllegalStateException("Failed to close payment client", exception);
                    } else {
                        failure.addSuppressed(exception);
                    }
                }
            }
        }
        this.clients.clear();
        this.contexts.clear();
        if (failure != null) {
            throw failure;
        }
    }

}
