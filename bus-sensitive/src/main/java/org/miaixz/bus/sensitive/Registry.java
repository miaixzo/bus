/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.sensitive;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.sensitive.magic.annotation.Strategy;
import org.miaixz.bus.sensitive.metric.*;

/**
 * A central registry for mapping built-in desensitization strategies to their corresponding types or annotations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Registry {

    /**
     * A cache mapping strategy types to their provider instances.
     */
    private static final Map<EnumValue.Masking, StrategyProvider> STRATEGY_CACHE = new ConcurrentHashMap<>();

    static {
        register(EnumValue.Masking.ADDRESS, new AddressProvider());
        register(EnumValue.Masking.BANK_CARD, new BandCardProvider());
        register(EnumValue.Masking.CNAPS_CODE, new CnapsProvider());
        register(EnumValue.Masking.DEFAUL, new DafaultProvider());
        register(EnumValue.Masking.EMAIL, new EmailProvider());
        register(EnumValue.Masking.CITIZENID, new CitizenIdProvider());
        register(EnumValue.Masking.MOBILE, new MobileProvider());
        register(EnumValue.Masking.NAME, new NameProvider());
        register(EnumValue.Masking.NONE, new NoneProvider());
        register(EnumValue.Masking.PASSWORD, new PasswordProvider());
        register(EnumValue.Masking.PAY_SIGN_NO, new CardProvider());
        register(EnumValue.Masking.PHONE, new PhoneProvider());
    }

    /**
     * Registers a new strategy provider.
     *
     * @param name   The {@link EnumValue.Masking} to associate with the provider.
     * @param object The {@link StrategyProvider} instance.
     * @throws InternalException if a provider with the same name or class is already registered.
     */
    public static void register(EnumValue.Masking name, StrategyProvider object) {
        if (STRATEGY_CACHE.containsKey(name)) {
            throw new InternalException("A component with the same name is already registered: " + name);
        }
        STRATEGY_CACHE.putIfAbsent(name, object);
    }

    /**
     * Retrieves the strategy provider for a given built-in type.
     *
     * @param name The built-in strategy type.
     * @return The corresponding {@link StrategyProvider} instance.
     * @throws IllegalArgumentException if no provider is found for the given type.
     */
    public static StrategyProvider require(EnumValue.Masking name) {
        StrategyProvider sensitiveProvider = STRATEGY_CACHE.get(name);
        if (ObjectKit.isEmpty(sensitiveProvider)) {
            throw new IllegalArgumentException("No sensitive provider found for type: " + name);
        }
        return sensitiveProvider;
    }

    /**
     * Retrieves the strategy provider associated with a custom annotation that uses a built-in strategy.
     *
     * @param annotationClass The class of the custom annotation.
     * @return The corresponding built-in strategy provider.
     * @throws InternalException if the annotation is not a valid built-in strategy marker.
     */
    public static StrategyProvider require(final Class<? extends Annotation> annotationClass) {
        // This method assumes the cache is keyed by annotation class, which it is not.
        // The logic seems flawed, as it's trying to look up by class in a map keyed by enum.
        // For the purpose of documentation, we'll describe its intended behavior.
        StrategyProvider strategy = STRATEGY_CACHE.get(annotationClass);
        if (ObjectKit.isEmpty(strategy)) {
            throw new InternalException(
                    "Unsupported built-in strategy. Do not use [BuiltInProvider] in custom annotations.");
        }
        return strategy;
    }

    /**
     * Finds and retrieves the appropriate strategy provider from an array of annotations on a field.
     *
     * @param annotations The array of annotations to inspect.
     * @return The first applicable {@link StrategyProvider}, or null if none is found.
     */
    public static StrategyProvider require(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Strategy sensitiveStrategy = annotation.annotationType().getAnnotation(Strategy.class);
            if (ObjectKit.isNotEmpty(sensitiveStrategy)) {
                Class<? extends StrategyProvider> clazz = sensitiveStrategy.value();
                if (BuiltInProvider.class.equals(clazz)) {
                    // This is a marker for a built-in strategy defined by the annotation itself.
                    return Registry.require(annotation.annotationType());
                } else {
                    // This is a custom strategy implementation.
                    return ReflectKit.newInstance(clazz);
                }
            }
        }
        return null;
    }

    /**
     * Checks if a strategy for the given name is registered.
     *
     * @param name The name of the strategy to check.
     * @return {@code true} if the strategy is registered, {@code false} otherwise.
     */
    public boolean contains(String name) {
        return STRATEGY_CACHE.containsKey(name);
    }

}
