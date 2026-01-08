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
package org.miaixz.bus.limiter.metric;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.limiter.magic.annotation.Limiting;

/**
 * Manages resources and their associated limiting protections for a specific user or context. This class tracks the
 * state of various resources, applying {@link Limiting} rules to control access and prevent overload.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResourceManager {

    /**
     * A static set to keep track of method resource keys that have been registered for protection. This is primarily
     * used for logging or to avoid re-registering rules.
     */
    private static final Set<String> PROTECTED_METHODS = new HashSet<>();
    /**
     * A concurrent hash map to store {@link Protection} instances, keyed by the resource identifier. Each entry
     * represents the current limiting state for a specific resource.
     */
    private final Map<String, Protection> map = new ConcurrentHashMap<>();

    /**
     * Checks if a given resource key is currently registered as a protected method.
     *
     * @param resourceKey The unique identifier of the resource.
     * @return {@code true} if the resource is protected, {@code false} otherwise.
     */
    public static boolean contain(String resourceKey) {
        return PROTECTED_METHODS.contains(resourceKey);
    }

    /**
     * Adds a resource key to the set of protected methods. This marks the resource as being under some form of limiting
     * protection.
     *
     * @param resourceKey The unique identifier of the resource to add.
     */
    public static void add(String resourceKey) {
        PROTECTED_METHODS.add(resourceKey);
    }

    /**
     * Attempts to gain entry to a resource, applying the specified limiting rules. If the resource is not yet managed,
     * a new {@link Protection} instance is created. It checks for expiration and remaining allowance before granting
     * access.
     *
     * @param resourceKey The unique identifier of the resource to access.
     * @param limiting    The {@link Limiting} annotation defining the rules for this resource.
     * @return {@code true} if access is allowed, {@code false} if access is denied due to limiting.
     * @throws IllegalStateException if the state of the protection is invalid (though current implementation does not
     *                               explicitly throw this).
     */
    public boolean entry(String resourceKey, Limiting limiting) throws IllegalStateException {
        Protection protection = map.get(resourceKey);

        // Cache operation: create a new Protection if one doesn't exist for the current resource
        if (Objects.isNull(protection)) {
            protection = new Protection(limiting);
            map.put(resourceKey, protection);
        }

        // Check if the protection period has expired
        if (protection.isExpire()) {
            protection.reset();
        }

        // Check if access is allowed based on the remaining count
        if (!protection.isAllow()) {
            return false;
        }

        protection.allowCount -= 1;
        return true;
    }

    /**
     * Checks if all managed resources within this {@code ResourceManager} are clear (i.e., their protection has expired
     * and they have been removed). This method iterates through the managed resources and removes expired ones.
     *
     * @return {@code true} if all resources are clear and the map is empty, {@code false} otherwise.
     */
    public boolean isClear() {
        map.keySet().forEach(key -> {
            Protection protection = map.get(key);
            if (Objects.nonNull(protection) && protection.isExpire()) {
                map.remove(key);
            }
        });
        return map.size() == 0;
    }

    /**
     * Represents the protection state for a single resource, including its limiting configuration and current access
     * allowance.
     */
    static class Protection {

        /**
         * The {@link Limiting} annotation object that defines the limiting rules for this resource.
         */
        Limiting limiting;
        /**
         * The {@link LocalDateTime} when the current limiting period for this resource will expire.
         */
        LocalDateTime targetTime;
        /**
         * The number of remaining requests allowed within the current limiting period.
         */
        int allowCount;

        /**
         * Constructs a new {@code Protection} instance with the given limiting configuration. It initializes the
         * protection state by resetting the allow count and target time.
         *
         * @param limiting The {@link Limiting} annotation that specifies the limiting rules.
         */
        public Protection(Limiting limiting) {
            this.limiting = limiting;
            this.reset();
        }

        /**
         * Checks if the current limiting period for this resource has expired.
         *
         * @return {@code true} if the {@link #targetTime} is in the past, {@code false} otherwise.
         */
        public boolean isExpire() {
            return targetTime.compareTo(LocalDateTime.now()) < 0;
        }

        /**
         * Resets the protection state, setting the {@link #allowCount} back to the configured limit and updating the
         * {@link #targetTime} for the next limiting period.
         */
        public void reset() {
            this.allowCount = limiting.count();
            this.targetTime = LocalDateTime.now().plusSeconds(limiting.duration());
        }

        /**
         * Checks if there are remaining requests allowed within the current limiting period.
         *
         * @return {@code true} if {@link #allowCount} is greater than 0, {@code false} otherwise.
         */
        public boolean isAllow() {
            return allowCount > 0;
        }
    }

}
