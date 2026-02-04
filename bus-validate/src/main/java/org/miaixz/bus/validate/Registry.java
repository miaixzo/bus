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
package org.miaixz.bus.validate;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.metric.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The validator registry center. This class manages the registration and retrieval of validator instances. It uses a
 * singleton pattern to ensure a single registry throughout the application.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Registry {

    /**
     * Cache for storing validator instances, keyed by both name and class simple name.
     */
    private static final Map<Object, Object> COMPLEX_CACHE = new ConcurrentHashMap<>();

    /**
     * The singleton instance of the registry.
     */
    private static Registry INSTANCE;

    /**
     * Static initializer to register all predefined validators.
     */
    static {
        register(Builder._ALWAYS, new AlwaysMatcher());
        register(Builder._BLANK, new BlankMatcher());
        register(Builder._CHINESE, new ChineseMatcher());
        register(Builder._CITIZENID, new CitizenIdMatcher());
        register(Builder._DATE, new DateMatcher());
        register(Builder._EACH, new EachMatcher());
        register(Builder._EMAIL, new EmailMatcher());
        register(Builder._ENGLISH, new EnglishMatcher());
        register(Builder._EQUALS, new EqualsMatcher());
        register(Builder._FALSE, new FalseMatcher());
        register(Builder._IN_ENUM, new InEnumMatcher());
        register(Builder._IN, new InMatcher());
        register(Builder._INT_RANGE, new IntRangeMatcher());
        register(Builder._IP_ADDRESS, new IPAddressMatcher());
        register(Builder._LENGTH, new LengthMatcher());
        register(Builder._MOBILE, new MobileMatcher());
        register(Builder._MULTI, new MultipleMatcher());
        register(Builder._NOT_BLANK, new NotBlankMatcher());
        register(Builder._NOT_EMPTY, new NotEmptyMatcher());
        register(Builder._NOT_IN, new NotInMatcher());
        register(Builder._NOT_NULL, new NotNullMatcher());
        register(Builder._NULL, new NullMatcher());
        register(Builder._PHONE, new PhoneMatcher());
        register(Builder._REFLECT, new ReflectMatcher());
        register(Builder._REGEX, new RegexMatcher());
        register(Builder._SIZE, new SizeMatcher());
        register(Builder._TRUE, new TrueMatcher());
    }

    /**
     * Private constructor to prevent direct instantiation.
     */
    public Registry() {

    }

    /**
     * Gets the singleton instance of the validator registry.
     *
     * @return the singleton {@code Registry} instance.
     */
    public static Registry getInstance() {
        synchronized (Registry.class) {
            if (ObjectKit.isEmpty(INSTANCE)) {
                INSTANCE = new Registry();
            }
        }
        return INSTANCE;
    }

    /**
     * Registers a validator component. The validator is registered under both its given name and its class's simple
     * name.
     *
     * @param name   the name of the component.
     * @param object the validator object to register.
     * @throws InternalException if a validator with the same name or type is already registered.
     */
    public static void register(String name, Object object) {
        if (COMPLEX_CACHE.containsKey(name)) {
            throw new InternalException("Duplicate registration of validator with the same name: " + name);
        }
        Class<?> clazz = object.getClass();
        if (COMPLEX_CACHE.containsKey(clazz.getSimpleName())) {
            throw new InternalException("Duplicate registration of validator with the same type: " + clazz);
        }
        COMPLEX_CACHE.putIfAbsent(name, object);
        COMPLEX_CACHE.putIfAbsent(clazz.getSimpleName(), object);
    }

    /**
     * Checks if a validator with the specified name is registered.
     *
     * @param name the name of the validator.
     * @return {@code true} if the validator is registered, {@code false} otherwise.
     */
    public boolean contains(String name) {
        return COMPLEX_CACHE.containsKey(name);
    }

    /**
     * Retrieves a validator by its name.
     *
     * @param name the name of the validator.
     * @return the validator object, or {@code null} if not found.
     */
    public Object require(String name) {
        return COMPLEX_CACHE.get(name);
    }

    /**
     * Retrieves a validator, first by name, and then by class type if not found by name.
     *
     * @param name  the name of the validator.
     * @param clazz the class of the validator.
     * @return the validator object, or {@code null} if not found by either name or class.
     */
    public Object require(String name, Class<?> clazz) {
        Object object = this.require(name);
        if (ObjectKit.isEmpty(object)) {
            object = this.require(clazz.getSimpleName());
        }
        return object;
    }

}
