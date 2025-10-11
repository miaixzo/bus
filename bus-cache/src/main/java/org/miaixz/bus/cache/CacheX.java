/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cache;

import java.util.Collection;
import java.util.Map;

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

}
