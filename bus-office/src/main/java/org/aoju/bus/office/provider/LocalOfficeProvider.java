/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.office.provider;

import com.sun.star.document.UpdateDocMode;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.builtin.AbstractJob;
import org.aoju.bus.office.builtin.AbstractNorm;
import org.aoju.bus.office.builtin.LocalMadeInOffice;
import org.aoju.bus.office.magic.family.FormatRegistry;
import org.aoju.bus.office.magic.filter.DefaultFilter;
import org.aoju.bus.office.magic.filter.Filter;
import org.aoju.bus.office.magic.filter.FilterChain;
import org.aoju.bus.office.metric.InstalledOfficeHolder;
import org.aoju.bus.office.metric.OfficeManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 文档转换器的默认实现。此实现将使用提供的office manager执行文档转换.
 * 必须启动所提供的office管理器才能被此转换器使用.
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public class LocalOfficeProvider extends AbstractProvider {

    /**
     * 如果不手动覆盖，则在加载文档时默认应用的属性.
     */
    public static final Map<String, Object> DEFAULT_LOAD_PROPERTIES;

    static {
        final Map<String, Object> loadProperties = new HashMap<>();
        loadProperties.put("Hidden", true);
        loadProperties.put("ReadOnly", true);
        loadProperties.put("UpdateDocMode", UpdateDocMode.QUIET_UPDATE);
        DEFAULT_LOAD_PROPERTIES = Collections.unmodifiableMap(loadProperties);
    }

    private final Map<String, Object> storeProperties;
    private Map<String, Object> loadProperties;
    private FilterChain filterChain;

    public LocalOfficeProvider(
            final OfficeManager officeManager,
            final FormatRegistry formatRegistry,
            final Map<String, Object> loadProperties,
            final FilterChain filterChain,
            final Map<String, Object> storeProperties) {
        super(officeManager, formatRegistry);

        this.loadProperties = loadProperties;
        this.filterChain = filterChain;
        this.storeProperties = storeProperties;
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
     * 使用默认配置创建一个新的{@link LocalOfficeProvider}。
     * 将要使用的{@link OfficeManager}是{@link InstalledOfficeHolder}类所包含的.
     *
     * @return 带有默认配置的 {@link LocalOfficeProvider}.
     */
    public static LocalOfficeProvider make() {
        return builder().build();
    }

    /**
     * 使用带有默认配置的指定的{@link OfficeManager}创建一个新的{@link LocalOfficeProvider}.
     *
     * @param officeManager 转换器将使用{@link OfficeManager}转换文档.
     * @return 带有默认配置的 {@link LocalOfficeProvider}.
     */
    public static LocalOfficeProvider make(final OfficeManager officeManager) {
        return builder().officeManager(officeManager).build();
    }

    @Override
    protected AbstractNorm convert(
            final AbstractSourceProvider source) {
        return new Local(source);
    }

    /**
     * 对于此转换器，设置在转换之前加载(打开)文档时使用的属性，而不管文档的输入类型.
     *
     * @param loadProperties 加载文档时应用的默认属性.
     * @return 这个转换器实例.
     */
    LocalOfficeProvider setLoadProperties(final Map<String, Object> loadProperties) {

        if (null == this.loadProperties) {
            this.loadProperties = new HashMap<>();
        }

        this.loadProperties.clear();
        this.loadProperties.putAll(loadProperties);

        return this;
    }

    /**
     * 对于此转换器，设置转换文档时应用的整个过滤器链.
     * FilterChain用于在转换之前(在加载文档之后)修改文档.
     * 过滤器的应用顺序与它们在链中的出现顺序相同.
     * 为了向后兼容，此函数只能由OfficeDocumentConverter调用.
     *
     * @param filterChain 在文档加载之后和以新文档格式存储(转换)之前应用的FilterChain.
     * @return 这个转换器实例.
     */
    LocalOfficeProvider setFilterChain(final FilterChain filterChain) {
        this.filterChain = filterChain;
        return this;
    }

    /**
     * 用于构造{@link LocalOfficeProvider}的生成器.
     *
     * @see LocalOfficeProvider
     */
    public static final class Builder extends AbstractConverterBuilder<Builder> {

        private Map<String, Object> loadProperties;
        private FilterChain filterChain;
        private Map<String, Object> storeProperties;

        private Builder() {
            super();
        }

        @Override
        public LocalOfficeProvider build() {
            return new LocalOfficeProvider(officeManager, formatRegistry, loadProperties, filterChain, storeProperties);
        }

        /**
         * 指定此转换器的加载属性，该属性将在转换任务期间加载文档时应用，而与文档的输入格式无关.
         * 使用此函数将替换默认的加载属性映射.
         *
         * @param properties 包含加载文档时要应用的属性的映射.
         * @return 这个构造器实例.
         */
        public Builder loadProperties(final Map<String, Object> properties) {
            this.loadProperties = properties;
            return this;
        }

        /**
         * 指定转换文档时应用的筛选器。可以使用筛选器在转换之前(在加载文档之后)修改文档.
         * 应用过滤器的顺序与它们作为参数出现的顺序相同.
         *
         * @param filters 将在文档加载之后和以新文档格式存储(转换)之前应用的筛选器.
         * @return 这个构造器实例.
         */
        public Builder filterChain(final Filter... filters) {
            this.filterChain = new DefaultFilter(filters);
            return this;
        }

        /**
         * 指定转换文档时应用的整个筛选器链。FilterChain用于在转换之前(在加载文档之后)修改文档.
         * 过滤器的应用顺序与它们在链中的出现顺序相同.
         *
         * @param filterChain 在文档加载之后和以新文档格式存储(转换)之前应用的FilterChain.
         * @return 这个构造器实例.
         */
        public Builder filterChain(final FilterChain filterChain) {
            this.filterChain = filterChain;
            return this;
        }

        /**
         * 指定在转换任务期间存储文档时将应用的属性.
         *
         * @param properties 包含在存储文档时要应用的自定义属性的映射.
         * @return 这个构造器实例.
         */
        public Builder storeProperties(final Map<String, Object> properties) {
            this.storeProperties = properties;
            return this;
        }

    }

    private class Local
            extends AbstractNorm {

        private Local(
                final AbstractSourceProvider source) {
            super(source, LocalOfficeProvider.this.officeManager, LocalOfficeProvider.this.formatRegistry);
        }

        @Override
        protected AbstractJob to(final AbstractTargetProvider target) {
            return new LocalJob(source, target);
        }

    }

    private class LocalJob extends AbstractJob {

        private LocalJob(
                final AbstractSourceProvider source, final AbstractTargetProvider target) {
            super(source, target);
        }

        @Override
        public void doExecute() throws InstrumentException {
            final LocalMadeInOffice task = new LocalMadeInOffice(source, target, loadProperties, filterChain, storeProperties);
            officeManager.execute(task);
        }

    }

}
