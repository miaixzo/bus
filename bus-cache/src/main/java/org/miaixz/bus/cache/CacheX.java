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
 * 缓存接口
 * <p>
 * 定义缓存系统的核心操作，包括读取、写入、检查、移除和清空缓存。 支持泛型，适用于不同类型的键值对，可实现内存缓存、分布式缓存等多种缓存机制。
 * </p>
 *
 * @param <K> 键的类型
 * @param <V> 值的类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CacheX<K, V> {

    /**
     * 从缓存中读取单个对象
     * <p>
     * 根据指定键获取缓存中的值。如果键不存在或值已过期，返回 <code>null</code>。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * User user = cache.read("user123");
     * if (user != null) {
     *     System.out.println("用户: " + user.getName());
     * }
     * }</pre>
     *
     * @param key 缓存键
     * @return 键对应的值，或 <code>null</code> 如果键不存在或值已过期
     */
    V read(K key);

    /**
     * 从缓存中批量读取多个对象
     * <p>
     * 根据指定键集合批量获取缓存中的值，返回包含键值对的映射。 不存在的键或已过期的值不会包含在返回的映射中。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * List<String> userIds = Arrays.asList("user123", "user456");
     * Map<String, User> users = cache.read(userIds);
     * users.forEach((id, user) -> System.out.println("ID: " + id + ", 名称: " + user.getName()));
     * }</pre>
     *
     * @param keys 键的集合
     * @return 包含键及其对应值的映射，可能为空
     */
    Map<K, V> read(Collection<K> keys);

    /**
     * 将对象写入缓存，使用默认过期时间
     * <p>
     * 将键值对写入缓存，采用默认过期时间（1小时，3600秒）。 如果键已存在，更新其值并重置过期时间。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * User user = new User("user123", "张三");
     * cache.write(user.getId(), user);
     * }</pre>
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    default void write(K key, V value) {
        write(key, value, 3600_000);
    }

    /**
     * 将对象写入缓存，指定过期时间
     * <p>
     * 将键值对写入缓存，并设置指定的过期时间（单位：毫秒）。 如果键已存在，更新其值并重置过期时间。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * User user = new User("user123", "张三");
     * cache.write(user.getId(), user, 30 * 60 * 1000); // 30分钟
     * }</pre>
     *
     * @param key    缓存键
     * @param value  缓存值
     * @param expire 过期时间（毫秒）
     */
    void write(K key, V value, long expire);

    /**
     * 批量写入多个对象到缓存，指定过期时间
     * <p>
     * 将多个键值对批量写入缓存，设置相同的过期时间（单位：毫秒）。 对于已存在的键，更新其值并重置过期时间。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * Map<String, User> userMap = new HashMap<>();
     * userMap.put("user123", new User("user123", "张三"));
     * userMap.put("user456", new User("user456", "李四"));
     * cache.write(userMap, 60 * 60 * 1000); // 1小时
     * }</pre>
     *
     * @param map    包含键值对的映射
     * @param expire 过期时间（毫秒）
     */
    void write(Map<K, V> map, long expire);

    /**
     * 检查缓存中是否存在未过期的键
     * <p>
     * 判断缓存中是否存在指定键且对应的值未过期。 默认实现返回 <code>true</code>，实现类应重写以提供具体逻辑。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * String userId = "user123";
     * if (cache.containsKey(userId)) {
     *     User user = cache.read(userId);
     *     System.out.println("缓存命中: " + user.getName());
     * }
     * }</pre>
     *
     * @param key 缓存键
     * @return <code>true</code> 如果键存在且值未过期；否则返回 <code>false</code>
     */
    default boolean containsKey(K key) {
        return true;
    }

    /**
     * 从缓存中移除指定键
     * <p>
     * 移除一个或多个指定键的缓存数据。对于不存在的键，操作将忽略。 支持可变参数以移除多个键。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * cache.remove("user123");
     * cache.remove("user456", "user789");
     * }</pre>
     *
     * @param keys 要移除的键
     */
    void remove(K... keys);

    /**
     * 清空缓存中的所有数据
     * <p>
     * 移除缓存中的所有键值对，使缓存变为空。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * CacheX<String, User> cache = new SomeCacheImpl<>();
     * cache.clear();
     * }</pre>
     */
    void clear();

}
