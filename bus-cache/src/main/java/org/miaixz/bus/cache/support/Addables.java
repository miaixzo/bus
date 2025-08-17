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
package org.miaixz.bus.cache.support;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.miaixz.bus.core.xyz.CollKit;

/**
 * 可添加对象工具类
 * <p>
 * 提供创建和操作数组、集合和Map的工具方法，支持初始化和批量添加元素。 通过工厂模式创建不同类型的可添加对象，并提供统一的操作接口。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Addables {

    /**
     * 创建新的可添加对象
     *
     * @param type 对象类型
     * @param size 初始大小
     * @return 可添加对象实例
     */
    public static Addable newAddable(Class<?> type, int size) {
        if (Map.class.isAssignableFrom(type)) {
            return new MapAddable().init((Class<Map>) type, size);
        } else if (Collection.class.isAssignableFrom(type)) {
            return new CollectionAddable().init((Class<Collection>) type, size);
        } else {
            return new ArrayAddable().init((Class<Object[]>) type, size);
        }
    }

    /**
     * 创建新的集合实例
     *
     * @param type           集合类型
     * @param initCollection 初始集合，用于初始化新集合
     * @return 新的集合实例
     */
    public static Collection newCollection(Class<?> type, Collection initCollection) {
        try {
            Collection collection = (Collection) type.getConstructor().newInstance();
            if (CollKit.isNotEmpty(initCollection)) {
                collection.addAll(initCollection);
            }
            return collection;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new RuntimeException(
                    "could not invoke collection: " + type.getName() + "'s no param (default) constructor!", e);
        }
    }

    /**
     * 创建新的Map实例
     *
     * @param type    Map类型
     * @param initMap 初始Map，用于初始化新Map
     * @return 新的Map实例
     */
    public static Map newMap(Class<?> type, Map initMap) {
        try {
            Map map = (Map) type.getConstructor().newInstance();
            if (CollKit.isNotEmpty(initMap)) {
                map.putAll(initMap);
            }
            return map;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new RuntimeException("could not invoke map: " + type.getName() + "'s no param (default) constructor!",
                    e);
        }
    }

    /**
     * 可添加对象接口
     * <p>
     * 定义了初始化、添加元素和获取结果的操作
     * </p>
     *
     * @param <T> 结果类型
     */
    public interface Addable<T> {
        /**
         * 初始化可添加对象
         *
         * @param type     对象类型
         * @param initSize 初始大小
         * @return 当前实例
         */
        Addable init(Class<T> type, int initSize);

        /**
         * 批量添加元素
         *
         * @param list 元素列表
         * @return 当前实例
         */
        Addable addAll(List<Object> list);

        /**
         * 获取结果
         *
         * @return 结果对象
         */
        T get();
    }

    /**
     * 数组可添加对象实现类
     */
    private static class ArrayAddable implements Addable<Object[]> {
        /**
         * 数组实例
         */
        private Object[] instance;

        /**
         * 初始化数组
         *
         * @param type     数组类型
         * @param initSize 初始大小
         * @return 当前实例
         */
        @Override
        public Addable init(Class<Object[]> type, int initSize) {
            this.instance = new Object[initSize];
            return this;
        }

        /**
         * 批量添加元素
         *
         * @param list 元素列表
         * @return 当前实例
         */
        @Override
        public Addable addAll(List<Object> list) {
            for (int i = 0; i < list.size(); ++i) {
                this.instance[i] = list.get(i);
            }
            return this;
        }

        /**
         * 获取结果
         *
         * @return 数组实例
         */
        @Override
        public Object[] get() {
            return this.instance;
        }
    }

    /**
     * 集合可添加对象实现类
     */
    private static class CollectionAddable implements Addable<Collection> {
        /**
         * 集合实例
         */
        private Collection instance;

        /**
         * 初始化集合
         *
         * @param type     集合类型
         * @param initSize 初始大小
         * @return 当前实例
         */
        @Override
        public Addable init(Class<Collection> type, int initSize) {
            try {
                this.instance = type.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                throw new RuntimeException(
                        "could not invoke collection: " + type.getName() + "'s no param (default) constructor!", e);
            }
            return this;
        }

        /**
         * 批量添加元素
         *
         * @param list 元素列表
         * @return 当前实例
         */
        @Override
        public Addable addAll(List<Object> list) {
            this.instance.addAll(list);
            return this;
        }

        /**
         * 获取结果
         *
         * @return 集合实例
         */
        @Override
        public Collection get() {
            return this.instance;
        }
    }

    /**
     * Map可添加对象实现类
     */
    private static class MapAddable implements Addable<Map> {
        /**
         * Map实例
         */
        private Map instance;

        /**
         * 初始化Map
         *
         * @param type     Map类型
         * @param initSize 初始大小
         * @return 当前实例
         */
        @Override
        public Addable init(Class<Map> type, int initSize) {
            try {
                this.instance = type.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                throw new RuntimeException(
                        "could not invoke Map: " + type.getName() + "'s no param (default) constructor!", e);
            }
            return this;
        }

        /**
         * 批量添加元素
         *
         * @param list 元素列表，每个元素应该是Map.Entry类型
         * @return 当前实例
         */
        @Override
        public Addable addAll(List<Object> list) {
            if (CollKit.isEmpty(list)) {
                return this;
            }
            list.stream().map(object -> (Map.Entry) object)
                    .forEach(entry -> instance.put(entry.getKey(), entry.getValue()));
            return this;
        }

        /**
         * 获取结果
         *
         * @return Map实例
         */
        @Override
        public Map get() {
            return instance;
        }
    }

}