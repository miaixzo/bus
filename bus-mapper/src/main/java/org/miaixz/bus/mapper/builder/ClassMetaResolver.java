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
package org.miaixz.bus.mapper.builder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.ORDER;

/**
 * 根据类型和方法等信息获取实体类类型，可通过 SPI 方式替换默认实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ClassMetaResolver extends ORDER {

    /**
     * 缓存，避免方法执行时每次都查找，键为 MapperTypeMethod，值为对应的实体类
     */
    Map<MapperTypeMethod, Optional<Class<?>>> ENTITY_CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * 查找当前方法对应的实体类
     *
     * @param mapperType   Mapper 接口，不能为空
     * @param mapperMethod Mapper 接口方法，可以为空
     * @return 实体类类型的 Optional 包装对象
     */
    static Optional<Class<?>> find(Class<?> mapperType, Method mapperMethod) {
        Objects.requireNonNull(mapperType);
        return ENTITY_CLASS_MAP.computeIfAbsent(new MapperTypeMethod(mapperType, mapperMethod), mapperTypeMethod -> {
            for (ClassMetaResolver instance : EntityClassFinderInstance.getInstances()) {
                Optional<Class<?>> optionalClass = instance.findEntityClass(mapperType, mapperMethod);
                if (optionalClass.isPresent()) {
                    return optionalClass;
                }
            }
            return Optional.empty();
        });
    }

    /**
     * 查找当前方法对应的实体类
     *
     * @param mapperType   Mapper 接口，不能为空
     * @param mapperMethod Mapper 接口方法，可以为空
     * @return 实体类类型的 Optional 包装对象
     */
    Optional<Class<?>> findEntityClass(Class<?> mapperType, Method mapperMethod);

    /**
     * 判断指定的类型是否为定义的实体类类型
     *
     * @param clazz 类型
     * @return true 表示是实体类类型，false 表示不是
     */
    boolean isEntityClass(Class<?> clazz);

    /**
     * Mapper 接口和方法，用作缓存 Key
     */
    class MapperTypeMethod {
        /**
         * Mapper 接口类
         */
        private final Class<?> mapperType;

        /**
         * Mapper 接口方法
         */
        private final Method mapperMethod;

        /**
         * 构造函数，初始化 Mapper 接口和方法
         *
         * @param mapperType   Mapper 接口类
         * @param mapperMethod Mapper 接口方法
         */
        public MapperTypeMethod(Class<?> mapperType, Method mapperMethod) {
            this.mapperType = mapperType;
            this.mapperMethod = mapperMethod;
        }

        /**
         * 判断两个 MapperTypeMethod 对象是否相等
         *
         * @param o 比较对象
         * @return true 表示相等，false 表示不相等
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            MapperTypeMethod that = (MapperTypeMethod) o;
            return Objects.equals(mapperType, that.mapperType) && Objects.equals(mapperMethod, that.mapperMethod);
        }

        /**
         * 计算对象的哈希值
         *
         * @return 哈希值
         */
        @Override
        public int hashCode() {
            return Objects.hash(mapperType, mapperMethod);
        }

        /**
         * 返回对象的字符串表示形式
         *
         * @return 字符串表示形式，格式为 "mapperTypeSimpleName.methodName" 或仅 "mapperTypeSimpleName."
         */
        @Override
        public String toString() {
            return (mapperType != null ? mapperType.getSimpleName() + "." : "")
                    + (mapperMethod != null ? mapperMethod.getName() : "");
        }
    }

    /**
     * 实体类查找器实例管理类
     */
    class EntityClassFinderInstance {
        /**
         * 缓存的 ClassFinder 实例列表
         */
        private static volatile List<ClassMetaResolver> INSTANCES;

        /**
         * 通过 SPI 获取扩展的实现或使用默认实现
         *
         * @return ClassFinder 实例列表
         */
        public static List<ClassMetaResolver> getInstances() {
            if (INSTANCES == null) {
                synchronized (ClassMetaResolver.class) {
                    if (INSTANCES == null) {
                        INSTANCES = NormalSpiLoader.loadList(false, ClassMetaResolver.class);
                    }
                }
            }
            return INSTANCES;
        }
    }

}