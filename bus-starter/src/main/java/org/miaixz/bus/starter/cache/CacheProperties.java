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
package org.miaixz.bus.starter.cache;

import java.time.Duration;
import java.util.Map;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Options;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable starter-side cache configuration bound from {@code bus.cache}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.CACHE)
public final class CacheProperties extends Options {

    /**
     * Whether the cache integration is enabled.
     */
    private final boolean enabled;
    /**
     * Maximum number of entries retained by the cache backend.
     */
    private final DataSize capacity;
    /**
     * Time-to-live applied to cache entries.
     */
    private final Duration expiry;
    /**
     * Named cache definitions keyed by logical cache name.
     */
    private final Map<String, CacheX> map;
    /**
     * Cache provider selected when no named override applies.
     */
    private final Collector provider;

    /**
     * Creates validated cache properties and initializes the core cache options.
     *
     * @param enabled  whether cache integration is enabled
     * @param type     backend type
     * @param capacity maximum in-process cache capacity
     * @param expiry   default cache expiry
     * @param nodes    memcached node list
     * @param redis    Redis connection options
     * @param map      explicitly supplied named cache instances
     * @param provider cache metrics collector configuration
     */
    public CacheProperties(@DefaultValue("false") boolean enabled, String type,
            @DefaultValue("10000B") DataSize capacity, @DefaultValue("1h") Duration expiry, String nodes,
            @DefaultValue Options.Redis redis, Map<String, CacheX> map, @DefaultValue Collector provider) {
        if (capacity == null || capacity.toBytes() <= 0) {
            throw new IllegalArgumentException("bus.cache.capacity must be greater than zero");
        }
        if (expiry == null || expiry.isZero() || expiry.isNegative()) {
            throw new IllegalArgumentException("bus.cache.expiry must be greater than zero");
        }
        this.enabled = enabled;
        this.capacity = capacity;
        this.expiry = expiry;
        this.map = map == null ? Map.of() : Map.copyOf(map);
        this.provider = provider == null ? new Collector(null, null, null, null) : provider;
        setType(type);
        setMaxSize(capacity.toBytes());
        setExpire(expiry.toMillis());
        setNodes(nodes);
        setRedis(redis == null ? new Options.Redis() : redis);
    }

    /**
     * Collects non-null cache backend specifications in stable order.
     *
     * Cache statistics collector configuration.
     *
     * @param key      lookup key
     * @param url      service endpoint URL
     * @param username authentication username
     * @param password authentication password
     */
    public record Collector(String key, String url, String username, String password) {

        /**
         * Exposes the provider-specific cache identifier.
         *
         * @return collector backend key
         */
        public String getKey() {
            return key;
        }

        /**
         * Exposes the remote cache service endpoint.
         *
         * @return JDBC URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * Exposes the username used to authenticate with the cache service.
         *
         * @return credential username reference
         */
        public String getUsername() {
            return username;
        }

        /**
         * Exposes the password used to authenticate with the cache service.
         *
         * @return credential password reference
         */
        public String getPassword() {
            return password;
        }

        /**
         * @return masked diagnostic text
         */
        @Override
        public String toString() {
            return "Collector[key=" + key + ", url=" + url + ", username=***, password=***]";
        }
    }

    /**
     * Returns a diagnostic representation without credentials or provider details.
     *
     * @return safe diagnostic text
     */
    @Override
    public String toString() {
        return "CacheProperties[enabled=" + enabled + ", capacity=" + capacity + ", expiry=" + expiry + ", namedCaches="
                + map.size() + ", provider=***]";
    }

}
