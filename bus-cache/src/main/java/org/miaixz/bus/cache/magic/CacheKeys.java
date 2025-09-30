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
package org.miaixz.bus.cache.magic;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 缓存键集合类
 * <p>
 * 用于存储批量缓存操作的结果，包括命中的键值对和未命中的键集合。 提供了获取命中键值对和未命中键集合的方法，并处理了null值情况。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheKeys {

    /**
     * 命中的键值对映射，键为缓存键，值为缓存值
     */
    private Map<String, Object> hitKeyMap;

    /**
     * 未命中的键集合
     */
    private Set<String> missKeySet;

    /**
     * 默认构造方法
     * <p>
     * 创建一个空的CacheKeys实例，hitKeyMap和missKeySet都为null
     * </p>
     */
    public CacheKeys() {
    }

    /**
     * 带参数的构造方法
     *
     * @param hitKeyMap  命中的键值对映射
     * @param missKeySet 未命中的键集合
     */
    public CacheKeys(Map<String, Object> hitKeyMap, Set<String> missKeySet) {
        this.hitKeyMap = hitKeyMap;
        this.missKeySet = missKeySet;
    }

    /**
     * 获取命中的键值对映射
     * <p>
     * 如果hitKeyMap为null，则返回一个空的不可修改映射
     * </p>
     *
     * @return 命中的键值对映射
     */
    public Map<String, Object> getHitKeyMap() {
        return null == hitKeyMap ? Collections.emptyMap() : hitKeyMap;
    }

    /**
     * 获取未命中的键集合
     * <p>
     * 如果missKeySet为null，则返回一个空的不可修改集合
     * </p>
     *
     * @return 未命中的键集合
     */
    public Set<String> getMissKeySet() {
        return null == missKeySet ? Collections.emptySet() : missKeySet;
    }

}
