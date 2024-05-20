/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.extra.template.provider.beetl;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;
import org.beetl.core.resource.*;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.template.Template;
import org.miaixz.bus.extra.template.TemplateConfig;
import org.miaixz.bus.extra.template.TemplateProvider;

import java.io.IOException;

/**
 * Beetl模板引擎封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeetlProvider implements TemplateProvider {

    private GroupTemplate engine;

    /**
     * 默认构造
     */
    public BeetlProvider() {
    }

    /**
     * 构造
     *
     * @param config 模板配置
     */
    public BeetlProvider(final TemplateConfig config) {
        init(config);
    }

    /**
     * 构造
     *
     * @param engine {@link GroupTemplate}
     */
    public BeetlProvider(final GroupTemplate engine) {
        init(engine);
    }

    /**
     * 创建引擎
     *
     * @param config 模板配置
     * @return {@link GroupTemplate}
     */
    private static GroupTemplate createEngine(TemplateConfig config) {
        if (null == config) {
            config = TemplateConfig.DEFAULT;
        }

        switch (config.getResourceMode()) {
            case CLASSPATH:
                return createGroupTemplate(new ClasspathResourceLoader(config.getPath(), config.getCharsetString()));
            case FILE:
                return createGroupTemplate(new FileResourceLoader(config.getPath(), config.getCharsetString()));
            case WEB_ROOT:
                return createGroupTemplate(new WebAppResourceLoader(config.getPath(), config.getCharsetString()));
            case STRING:
                return createGroupTemplate(new StringTemplateResourceLoader());
            case COMPOSITE:
                //TODO 需要定义复合资源加载器
                return createGroupTemplate(new CompositeResourceLoader());
            default:
                return new GroupTemplate();
        }
    }

    /**
     * 创建自定义的模板组 {@link GroupTemplate}，配置文件使用全局默认
     * 此时自定义的配置文件可在ClassPath中放入beetl.properties配置
     *
     * @param loader {@link ResourceLoader}，资源加载器
     * @return {@link GroupTemplate}
     */
    private static GroupTemplate createGroupTemplate(final ResourceLoader<?> loader) {
        try {
            return createGroupTemplate(loader, Configuration.defaultConfiguration());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建自定义的 {@link GroupTemplate}
     *
     * @param loader {@link ResourceLoader}，资源加载器
     * @param conf   {@link Configuration} 配置文件
     * @return {@link GroupTemplate}
     */
    private static GroupTemplate createGroupTemplate(final ResourceLoader<?> loader, final Configuration conf) {
        return new GroupTemplate(loader, conf);
    }

    @Override
    public TemplateProvider init(final TemplateConfig config) {
        init(createEngine(config));
        return this;
    }

    /**
     * 初始化引擎
     *
     * @param engine 引擎
     */
    private void init(final GroupTemplate engine) {
        this.engine = engine;
    }

    @Override
    public Template getTemplate(final String resource) {
        if (null == this.engine) {
            init(TemplateConfig.DEFAULT);
        }
        return BeetlTemplate.wrap(engine.getTemplate(resource));
    }

    /**
     * 获取原始引擎的钩子方法，用于自定义特殊属性，如插件等
     *
     * @return {@link GroupTemplate}
     */
    @Override
    public GroupTemplate getRaw() {
        return this.engine;
    }
}
