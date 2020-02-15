/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.magic.family;

import org.aoju.bus.core.builder.ToStringBuilder;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 包含处理特定文档格式所需的信息
 *
 * @author Kimi Liu
 * @version 5.6.1
 * @since JDK 1.8+
 */
public class DocumentFormat {

    public String name;
    public List<String> extensions;
    public String mediaType;
    public FamilyType inputFamily;
    public Map<String, Object> loadProperties;
    public Map<FamilyType, Map<String, Object>> storeProperties;

    public DocumentFormat() {

    }

    /**
     * 使用指定的名称、扩展名和mime类型创建新的只读文档格式
     *
     * @param name            格式的名称.
     * @param extensions      格式的文件名扩展名.
     * @param mediaType       格式的媒体类型(mime类型).
     * @param inputFamily     文档的DocumentFamily.
     * @param loadProperties  加载(打开)这种格式的文档所需的属性.
     * @param storeProperties 将这种格式的文档存储(保存)到另一个集合文档所需的属性.
     * @param unmodifiable    {@code true} 文档在创建后不能修改格式, {@code false}尚未创建则可以修改.
     */
    private DocumentFormat(
            final String name,
            final Collection<String> extensions,
            final String mediaType,
            final FamilyType inputFamily,
            final Map<String, Object> loadProperties,
            final Map<FamilyType, Map<String, Object>> storeProperties,
            final boolean unmodifiable) {

        this.name = name;
        this.extensions = new ArrayList<>(extensions);
        this.mediaType = mediaType;
        this.inputFamily = inputFamily;
        this.loadProperties = Optional.ofNullable(loadProperties)
                .map(HashMap<String, Object>::new)
                .map(mapCopy -> unmodifiable ? Collections.unmodifiableMap(mapCopy) : mapCopy)
                .orElse(null);
        this.storeProperties = Optional.ofNullable(storeProperties)
                .map(map -> {
                    final EnumMap<FamilyType, Map<String, Object>> familyMap =
                            new EnumMap<>(FamilyType.class);
                    map.forEach((family, propMap) -> familyMap.put(
                            family,
                            unmodifiable
                                    ? Collections.unmodifiableMap(new HashMap<>(propMap))
                                    : new HashMap<>(propMap)));
                    return familyMap;
                })
                .map(mapCopy -> unmodifiable ? Collections.unmodifiableMap(mapCopy) : mapCopy)
                .orElse(null);
    }

    /**
     * 创建一个新的生成器实例.
     *
     * @return 新的生成器实例.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 从指定的格式创建一个新的可修改的{@link DocumentFormat}.
     *
     * @param sourceFormat 源文档格式.
     * @return {@link DocumentFormat}，与默认的文档格式不同，它是可修改的.
     */
    public static DocumentFormat copy(final DocumentFormat sourceFormat) {
        return new Builder().from(sourceFormat).unmodifiable(false).build();
    }

    /**
     * 从指定格式创建一个新的不可修改的{@link DocumentFormat}.
     *
     * @param sourceFormat 源文档格式.
     * @return {@link DocumentFormat}，与默认的文档格式不同，它是可修改的.
     */
    public static DocumentFormat unmodifiableCopy(final DocumentFormat sourceFormat) {
        return new Builder().from(sourceFormat).unmodifiable(true).build();
    }

    /**
     * 获取与文档格式关联的扩展名。它将返回与{@code #getExtensions().get(0)}相同的扩展名.
     *
     * @return 表示扩展名的字符串.
     */
    public String getExtension() {
        return extensions.get(0);
    }

    /**
     * 获取文档格式的文件名扩展名.
     *
     * @return 表示扩展的字符串列表.
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * 获取文档格式的输入DocumentFamily.
     *
     * @return 文档格式.
     */
    public FamilyType getInputFamily() {
        return inputFamily;
    }

    /**
     * 获取加载(打开)这种格式文档所需的属性.
     *
     * @return 包含加载这种格式的文档时要应用的属性的映射.
     */
    public Map<String, Object> getLoadProperties() {
        return loadProperties;
    }

    /**
     * 获取格式的媒体(mime)类型.
     *
     * @return 表示媒体类型的字符串.
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * 获取格式的名称.
     *
     * @return 表示格式名称的字符串.
     */
    public String getName() {
        return name;
    }

    /**
     * 获取将这种格式的文档存储(保存)到受支持的集合文档所需的属性.
     *
     * @return DocumentFamily/Map对，包含在按DocumentFamily存储这种格式的文档时要应用的属性.
     */
    public Map<FamilyType, Map<String, Object>> getStoreProperties() {
        return storeProperties;
    }

    /**
     * 从指定集合的文档中获取将文档存储(保存)为这种格式所需的属性.
     *
     * @param family 属性获取的DocumentFamily.
     * @return 包含将文档存储为这种格式时要应用的属性的映射.
     */
    public Map<String, Object> getStoreProperties(final FamilyType family) {
        return storeProperties == null ? null : storeProperties.get(family);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


    public static final class Builder {

        private String name;
        private Set<String> extensions;
        private String mediaType;
        private FamilyType inputFamily;
        private Map<String, Object> loadProperties;
        private Map<FamilyType, Map<String, Object>> storeProperties;
        private boolean unmodifiable = true;

        private Builder() {
            super();
        }

        public DocumentFormat build() {
            return new DocumentFormat(
                    name, extensions, mediaType, inputFamily, loadProperties, storeProperties, unmodifiable);
        }

        /**
         * 通过复制指定文档格式的属性来初始化生成器.
         *
         * @param sourceFormat 源文档格式不能为空.
         * @return 当前构造器实例.
         */
        public Builder from(final DocumentFormat sourceFormat) {
            this.name = sourceFormat.getName();
            this.extensions = new LinkedHashSet<>(sourceFormat.getExtensions());
            this.mediaType = sourceFormat.getMediaType();
            this.inputFamily = sourceFormat.getInputFamily();
            this.loadProperties = Optional.ofNullable(sourceFormat.getLoadProperties())
                    .map(map -> map.entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    .orElse(null);
            this.storeProperties =
                    Optional.ofNullable(sourceFormat.getStoreProperties())
                            .map(map -> {
                                final EnumMap<FamilyType, Map<String, Object>> familyMap =
                                        new EnumMap<>(FamilyType.class);
                                map.forEach((family, propMap) -> familyMap.put(family, new HashMap<>(propMap)));
                                return familyMap;
                            })
                            .orElse(null);

            return this;
        }

        /**
         * 指定与文档格式关联的扩展名.
         *
         * @param extension 扩展名不能为空.
         * @return 当前构造器实例.
         */
        public Builder extension(final String extension) {
            if (CollUtils.isEmpty(this.extensions)) {
                this.extensions = new LinkedHashSet<>();
            }
            this.extensions.add(extension);
            return this;
        }

        /**
         * 指定与文档格式关联的输入(文档加载时)DocumentFamily.
         *
         * @param inputFamily DocumentFamily不能为空.
         * @return 当前构造器实例.
         */
        public Builder inputFamily(final FamilyType inputFamily) {
            this.inputFamily = inputFamily;
            return this;
        }

        /**
         * 向生成器添加一个属性，该属性将在加载(打开)这种格式的文档时应用.
         *
         * @param name  属性名不能为空.
         * @param value 属性值可以为空。如果为空，它将从映射中删除该属性.
         * @return 当前构造器实例.
         */
        public Builder loadProperty(final String name, final Object value) {
            if (ObjectUtils.isEmpty(value)) {
                Optional.ofNullable(loadProperties).ifPresent(propMap -> propMap.remove(name));
            } else {
                if (ObjectUtils.isEmpty(this.loadProperties)) {
                    this.loadProperties = new HashMap<>();
                }
                this.loadProperties.put(name, value);
            }
            return this;
        }

        /**
         * 指定文档格式的媒体(mime)类型
         *
         * @param mediaType 表示媒体类型的字符串不能为空.
         * @return 当前构造器实例.
         */
        public Builder mediaType(final String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        /**
         * 指定文档格式的名称.
         *
         * @param name 文档格式的名称不能为空.
         * @return 当前构造器实例.
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * 指定文档格式在创建后是否不可修改。默认为{@code true}.
         *
         * @param unmodifiable {@code true} 文档在创建后不能修改格式, {@code false}尚未创建则可以修改.
         * @return 当前构造器实例.
         */
        public Builder unmodifiable(final boolean unmodifiable) {
            this.unmodifiable = unmodifiable;
            return this;
        }

        /**
         * 向生成器添加一个属性，该属性将在从指定集合的文档存储(保存)为这种格式的文档时应用.
         *
         * @param family 源(加载的)文档的文档族不能为空.
         * @param name   文档格式的名称不能为空.
         * @param value  属性值可以为空。如果为空，它将从映射中删除该属性.
         * @return 当前构造器实例.
         */
        public Builder storeProperty(
                final FamilyType family,
                final String name,
                final Object value) {
            if (value == null) {
                Optional.ofNullable(storeProperties).map(familyMap ->
                        familyMap.get(family)).ifPresent(propMap -> propMap.remove(name));
            } else {
                if (storeProperties == null) {
                    storeProperties = new EnumMap<>(FamilyType.class);
                }
                storeProperties.computeIfAbsent(family, key -> new HashMap<>()).put(name, value);
            }
            return this;
        }
    }

}
