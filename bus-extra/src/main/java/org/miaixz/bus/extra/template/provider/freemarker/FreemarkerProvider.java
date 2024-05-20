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
package org.miaixz.bus.extra.template.provider.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.extra.template.Template;
import org.miaixz.bus.extra.template.TemplateConfig;
import org.miaixz.bus.extra.template.TemplateProvider;

import java.io.IOException;

/**
 * FreeMarker模板引擎封装
 * 见：<a href="https://freemarker.apache.org/">https://freemarker.apache.org/</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FreemarkerProvider implements TemplateProvider {

    private Configuration cfg;

    /**
     * 默认构造
     */
    public FreemarkerProvider() {
    }

    /**
     * 构造
     *
     * @param config 模板配置
     */
    public FreemarkerProvider(final TemplateConfig config) {
        init(config);
    }

    /**
     * 构造
     *
     * @param freemarkerCfg {@link Configuration}
     */
    public FreemarkerProvider(final Configuration freemarkerCfg) {
        init(freemarkerCfg);
    }

    /**
     * 创建配置项
     *
     * @param config 模板配置
     * @return {@link Configuration }
     */
    private static Configuration createCfg(TemplateConfig config) {
        if (null == config) {
            config = new TemplateConfig();
        }

        final Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setLocalizedLookup(false);
        cfg.setDefaultEncoding(config.getCharset().toString());

        switch (config.getResourceMode()) {
            case CLASSPATH:
                cfg.setTemplateLoader(new ClassTemplateLoader(ClassKit.getClassLoader(), config.getPath()));
                break;
            case FILE:
                try {
                    cfg.setTemplateLoader(new FileTemplateLoader(FileKit.file(config.getPath())));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
                break;
            case WEB_ROOT:
                try {
                    cfg.setTemplateLoader(new FileTemplateLoader(FileKit.file(FileKit.getWebRoot(), config.getPath())));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
                break;
            case STRING:
                cfg.setTemplateLoader(new SimpleStringTemplateLoader());
                break;
            default:
                break;
        }

        return cfg;
    }

    @Override
    public TemplateProvider init(TemplateConfig config) {
        if (null == config) {
            config = TemplateConfig.DEFAULT;
        }
        init(createCfg(config));
        return this;
    }

    /**
     * 初始化引擎
     *
     * @param freemarkerCfg Configuration
     */
    private void init(final Configuration freemarkerCfg) {
        this.cfg = freemarkerCfg;
    }

    @Override
    public Template getTemplate(final String resource) {
        if (null == this.cfg) {
            init(TemplateConfig.DEFAULT);
        }
        try {
            return FreemarkerTemplate.wrap(this.cfg.getTemplate(resource));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取原始引擎的钩子方法，用于自定义特殊属性，如插件等
     *
     * @return {@link Configuration}
     */
    @Override
    public Configuration getRaw() {
        return this.cfg;
    }
}
