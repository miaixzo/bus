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
package org.miaixz.bus.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * Cache Interface.
 * <p>
 * Defines the core operations for a caching system, including reading, writing, checking, removing, and clearing the
 * cache. Supports generics for different types of key-value pairs and can be implemented for various caching mechanisms
 * such as in-memory cache, distributed cache, etc.
 * </p>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CacheX<K, V> {

    /**
     * Reads a single object from the cache.
     * <p>
     * Retrieves the value from the cache based on the specified key. Returns <code>null</code> if the key does not
     * exist or the value has expired. Example code:
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * User user = cache.read("user123");
     * if (user != null) {
     *     System.out.println("User: " + user.getName());
     * }
     * }</pre>
     *
     * @param key the cache key
     * @return the value corresponding to the key, or <code>null</code> if the key does not exist or the value has
     *         expired
     */
    V read(K key);

    /**
     * Reads multiple objects from the cache in a batch.
     * <p>
     * Retrieves values from the cache based on the specified collection of keys, returning a map of key-value pairs.
     * Keys that do not exist or have expired values will not be included in the returned map. Example code:
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * List<String> userIds = Arrays.asList("user123", "user456");
     * Map<String, User> users = cache.read(userIds);
     * users.forEach((id, user) -> System.out.println("ID: " + id + ", Name: " + user.getName()));
     * }</pre>
     *
     * @param keys a collection of keys
     * @return a map containing the keys and their corresponding values, which may be empty
     */
    Map<K, V> read(Collection<K> keys);

    /**
     * Writes an object to the cache with the default expiration time.
     * <p>
     * Writes a key-value pair to the cache with a default expiration time (1 hour, 3600 seconds). If the key already
     * exists, its value is updated and the expiration time is reset. Example code:
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * User user = new User("user123", "John Doe");
     * cache.write(user.getId(), user);
     * }</pre>
     *
     * @param key   the cache key
     * @param value the cache value
     */
    default void write(K key, V value) {
        write(key, value, 3600_000);
    }

    /**
     * Writes an object to the cache with a specified expiration time.
     * <p>
     * Writes a key-value pair to the cache and sets a specified expiration time in milliseconds. If the key already
     * exists, its value is updated and the expiration time is reset. Example code:
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * User user = new User("user123", "John Doe");
     * cache.write(user.getId(), user, 30 * 60 * 1000); // 30 minutes
     * }</pre>
     *
     * @param key    the cache key
     * @param value  the cache value
     * @param expire the expiration time in milliseconds
     */
    void write(K key, V value, long expire);

    /**
     * Writes multiple objects to the cache in a batch with a specified expiration time.
     * <p>
     * Writes multiple key-value pairs to the cache in a batch, setting the same expiration time in milliseconds for
     * all. For existing keys, their values are updated and the expiration time is reset. Example code:
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * Map<String, User> userMap = new HashMap<>();
     * userMap.put("user123", new User("user123", "John Doe"));
     * userMap.put("user456", new User("user456", "Jane Smith"));
     * cache.write(userMap, 60 * 60 * 1000); // 1 hour
     * }</pre>
     *
     * @param map    a map containing key-value pairs
     * @param expire the expiration time in milliseconds
     */
    void write(Map<K, V> map, long expire);

    /**
     * Checks if an unexpired key exists in the cache.
     * <p>
     * Determines if the specified key exists in the cache and its corresponding value has not expired. The default
     * implementation returns <code>true</code>; implementing classes should override this to provide specific logic.
     * Example code:
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * String userId = "user123";
     * if (cache.containsKey(userId)) {
     *     User user = cache.read(userId);
     *     System.out.println("Cache hit: " + user.getName());
     * }
     * }</pre>
     *
     * @param key the cache key
     * @return <code>true</code> if the key exists and its value is not expired; otherwise, returns <code>false</code>
     */
    default boolean containsKey(K key) {
        return true;
    }

    /**
     * Removes specified keys from the cache.
     * <p>
     * Removes the cached data for one or more specified keys. The operation is ignored for keys that do not exist.
     * Supports varargs to remove multiple keys. Example code:
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * cache.remove("user123");
     * cache.remove("user456", "user789");
     * }</pre>
     *
     * @param keys the keys to be removed
     */
    void remove(K... keys);

    /**
     * Clears all data from the cache.
     * <p>
     * Removes all key-value pairs from the cache, making it empty. Example code:
     * </p>
     *
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * cache.clear();
     * }</pre>
     */
    void clear();

    /**
     * Scans and returns all key-value pairs whose keys start with the given prefix.
     * <p>
     * Implementations that support prefix-based storage (e.g., memory, Redis, JDBC) should override this method to
     * provide efficient scanning. The default implementation returns an empty map. Example code:
     * </p>
     *
     * <pre>{@code
     * CacheX<String, String> store = new MemoryCache<>();
     * store.write("registry:default:API:001", "{}");
     * store.write("registry:default:API:002", "{}");
     * Map<String, String> entries = store.scan("registry:default:API:");
     * }</pre>
     *
     * @param prefix the key prefix to match
     * @return a map of all matching key-value pairs; returns an empty map if not supported or no matches found
     */
    default Map<K, V> scan(K prefix) {
        return MapKit.empty();
    }

    /**
     * Returns all keys that start with the given prefix, without loading values.
     * <p>
     * This is a lightweight alternative to {@link #scan(Object)} when only keys are needed. The default implementation
     * delegates to {@link #scan(Object)} and extracts the key set.
     * </p>
     *
     * @param prefix the key prefix to match
     * @return a list of matching keys; returns an empty list if not supported or no matches found
     */
    default List<K> keys(K prefix) {
        return ListKit.of(scan(prefix).keySet());
    }

    /**
     * Atomically increments the numeric value stored at the given key and returns the new value.
     * <p>
     * If the key does not exist it is created with an initial value of {@code 1}. Implementations must guarantee
     * atomicity:
     * <ul>
     * <li>Memory: backed by {@code AtomicLong}</li>
     * <li>Redis: uses the {@code INCR} command</li>
     * <li>JDBC: uses {@code UPDATE … SET val = val + 1 RETURNING val} (or equivalent)</li>
     * </ul>
     * Used by {@code Sequence} (config version numbers) and {@code IdGenerator} (Snowflake worker-id allocation).
     *
     * @param key the cache key holding the counter
     * @return the new value after increment
     */
    default long increment(K key) {
        throw new UnsupportedOperationException("increment() is not supported by this CacheX implementation");
    }

}
