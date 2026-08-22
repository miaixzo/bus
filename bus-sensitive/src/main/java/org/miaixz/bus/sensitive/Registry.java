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
package org.miaixz.bus.sensitive;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.sensitive.magic.annotation.Strategy;
import org.miaixz.bus.sensitive.nimble.*;

/**
 * A central registry for mapping built-in desensitization strategies to their corresponding types or annotations.
 *
 * @author Kimi Liu
 */
public class Registry {

    /**
     * Constructs a new Registry instance.
     */
    public Registry() {
        // No initialization required.
    }

    /**
     * A cache mapping strategy types to their provider instances.
     */
    private static final Map<EnumValue.Masking, StrategyProvider> STRATEGY_CACHE = new ConcurrentHashMap<>();

    static {
        register(new AddressProvider());
        register(new BandCardProvider());
        register(new CnapsProvider());
        register(new DafaultProvider());
        register(new EmailProvider());
        register(new CitizenIdProvider());
        register(new MobileProvider());
        register(new NameProvider());
        register(new NoneProvider());
        register(new PasswordProvider());
        register(new CardProvider());
        register(new PhoneProvider());
    }

    /**
     * Registers a new strategy provider.
     *
     * @param provider The {@link StrategyProvider} instance.
     * @throws IllegalArgumentException if the provider or its masking strategy is null
     * @throws InternalException        if another provider is already registered for the same masking strategy
     */
    public static void register(StrategyProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Sensitive provider must not be null");
        }
        final EnumValue.Masking type = provider.type();
        if (type == null) {
            throw new IllegalArgumentException(
                    "Sensitive provider type must not be null: " + provider.getClass().getName());
        }
        Logger.debug(
                true,
                "Sensitive",
                "Sensitive strategy registration started: type={}, provider={}",
                type,
                provider.getClass().getSimpleName());
        final StrategyProvider previous = STRATEGY_CACHE.putIfAbsent(type, provider);
        if (previous != null) {
            Logger.warn(
                    false,
                    "Sensitive",
                    "Sensitive strategy registration rejected: type={}, reason=duplicateName",
                    type);
            throw new InternalException("A sensitive provider is already registered for type: " + type);
        }
        Logger.debug(
                false,
                "Sensitive",
                "Sensitive strategy registered: type={}, provider={}, registeredCount={}",
                type,
                provider.getClass().getSimpleName(),
                STRATEGY_CACHE.size());
    }

    /**
     * Retrieves the strategy provider for a given built-in type.
     *
     * @param name The built-in strategy type.
     * @return The corresponding {@link StrategyProvider} instance.
     * @throws IllegalArgumentException if no provider is found for the given type.
     */
    public static StrategyProvider require(EnumValue.Masking name) {
        if (name == null) {
            throw new IllegalArgumentException("Sensitive strategy type must not be null");
        }
        Logger.debug(true, "Sensitive", "Sensitive strategy lookup started: type={}", name);
        StrategyProvider sensitiveProvider = STRATEGY_CACHE.get(name);
        if (ObjectKit.isEmpty(sensitiveProvider)) {
            Logger.warn(false, "Sensitive", "Sensitive strategy lookup failed: type={}", name);
            throw new IllegalArgumentException("No sensitive provider found for type: " + name);
        }
        Logger.debug(
                false,
                "Sensitive",
                "Sensitive strategy resolved: type={}, provider={}",
                name,
                sensitiveProvider.getClass().getSimpleName());
        return sensitiveProvider;
    }

    /**
     * Rejects an unresolved built-in marker because an annotation class alone does not declare the
     * {@link EnumValue.Masking} key required by this registry.
     *
     * @param annotationClass custom annotation using the unresolved marker
     * @return never returns normally
     * @throws IllegalArgumentException if the annotation type is null
     * @throws InternalException        always, because no masking key can be derived from the marker
     */
    public static StrategyProvider require(final Class<? extends Annotation> annotationClass) {
        Logger.debug(
                true,
                "Sensitive",
                "Sensitive annotation strategy lookup started: annotation={}",
                annotationClass == null ? null : annotationClass.getName());
        if (annotationClass == null) {
            throw new IllegalArgumentException("Sensitive strategy annotation type must not be null");
        }
        Logger.warn(
                false,
                "Sensitive",
                "Sensitive annotation strategy lookup failed: annotation={}, reason=missingMaskingType",
                annotationClass.getName());
        throw new InternalException(
                "BuiltInProvider is only a marker and does not declare a masking type; use a concrete StrategyProvider");
    }

    /**
     * Finds and retrieves the appropriate strategy provider from an array of annotations on a field.
     *
     * @param annotations The array of annotations to inspect.
     * @return The first applicable {@link StrategyProvider}, or null if none is found.
     */
    public static StrategyProvider require(final Annotation[] annotations) {
        Logger.debug(
                true,
                "Sensitive",
                "Sensitive strategy lookup started: annotationCount={}",
                annotations == null ? 0 : annotations.length);
        for (Annotation annotation : annotations) {
            Strategy sensitiveStrategy = annotation.annotationType().getAnnotation(Strategy.class);
            if (ObjectKit.isNotEmpty(sensitiveStrategy)) {
                Class<? extends StrategyProvider> clazz = sensitiveStrategy.value();
                if (BuiltInProvider.class.equals(clazz)) {
                    // This is a marker for a built-in strategy defined by the annotation itself.
                    Logger.debug(
                            true,
                            "Sensitive",
                            "Sensitive built-in strategy selected: annotation={}",
                            annotation.annotationType().getName());
                    return Registry.require(annotation.annotationType());
                } else {
                    // This is a custom strategy implementation.
                    Logger.debug(
                            true,
                            "Sensitive",
                            "Sensitive custom strategy selected: annotation={}, provider={}",
                            annotation.annotationType().getName(),
                            clazz.getName());
                    return ReflectKit.newInstance(clazz);
                }
            }
        }
        Logger.debug(
                false,
                "Sensitive",
                "Sensitive strategy lookup completed: found=false, annotationCount={}",
                annotations == null ? 0 : annotations.length);
        return null;
    }

    /**
     * Checks if a strategy for the given name is registered.
     *
     * @param type masking strategy to check
     * @return {@code true} if the strategy is registered, {@code false} otherwise.
     */
    public static boolean contains(EnumValue.Masking type) {
        return type != null && STRATEGY_CACHE.containsKey(type);
    }

}
