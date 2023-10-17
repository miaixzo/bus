/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.shade.screw.engine;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.shade.screw.Builder;
import org.aoju.bus.shade.screw.metadata.DataSchema;

import java.io.*;
import java.util.Locale;
import java.util.Objects;

/**
 * freemarker
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FreemarkerEngine extends AbstractEngine {
    /**
     * freemarker 配置实例化
     */
    private final Configuration configuration = new Configuration(
            Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

    {
        try {
            String path = getEngineConfig().getCustomTemplate();
            //自定义模板
            if (StringKit.isNotBlank(path) && FileKit.exists(path)) {
                //获取父目录
                String parent = Objects.requireNonNull(FileKit.file(path)).getParent();
                //设置模板加载路径
                configuration.setDirectoryForTemplateLoading(new File(parent));
            }
            //加载自带模板
            else {
                //模板存放路径
                configuration.setTemplateLoader(
                        new ClassTemplateLoader(this.getClass(), TemplateType.FREEMARKER.getTemplateDir()));
            }
            //编码
            configuration.setDefaultEncoding(Charset.DEFAULT_UTF_8);
            //国际化
            configuration.setLocale(new Locale(Builder.DEFAULT_LOCALE));
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    public FreemarkerEngine(EngineConfig templateConfig) {
        super(templateConfig);
    }

    /**
     * 生成文档
     *
     * @param info {@link DataSchema}
     * @throws InternalException 异常
     */
    @Override
    public void produce(DataSchema info, String docName) throws InternalException {
        Assert.notNull(info, "DataModel can not be empty!");
        String path = getEngineConfig().getCustomTemplate();
        try {
            Template template;
            // freemarker template
            // 如果自定义路径不为空文件也存在
            if (StringKit.isNotBlank(path) && FileKit.exists(path)) {
                // 文件名称
                String fileName = new File(path).getName();
                template = configuration.getTemplate(fileName);
            }
            //获取系统默认的模板
            else {
                template = configuration
                        .getTemplate(getEngineConfig().getFileType().getTemplateNamePrefix()
                                + TemplateType.FREEMARKER.getSuffix());
            }
            // create file
            File file = getFile(docName);
            // writer freemarker
            try (Writer out = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), Charset.UTF_8))) {
                // process
                template.process(info, out);
                // open the output directory
                openOutputDir();
            }
        } catch (IOException | TemplateException e) {
            throw new InternalException(e);
        }
    }

}
