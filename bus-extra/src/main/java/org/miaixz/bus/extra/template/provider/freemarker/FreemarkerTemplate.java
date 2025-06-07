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
package org.miaixz.bus.extra.template.provider.freemarker;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.template.Template;

/**
 * Freemarker模板实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FreemarkerTemplate implements Template, Serializable {

    @Serial
    private static final long serialVersionUID = 2852288838528L;

    freemarker.template.Template rawTemplate;

    /**
     * 构造
     *
     * @param freemarkerTemplate Beetl的模板对象 {@link freemarker.template.Template}
     */
    public FreemarkerTemplate(final freemarker.template.Template freemarkerTemplate) {
        this.rawTemplate = freemarkerTemplate;
    }

    /**
     * 包装Freemarker模板
     *
     * @param beetlTemplate Beetl的模板对象 {@link freemarker.template.Template}
     * @return this
     */
    public static FreemarkerTemplate wrap(final freemarker.template.Template beetlTemplate) {
        return (null == beetlTemplate) ? null : new FreemarkerTemplate(beetlTemplate);
    }

    @Override
    public void render(final Map<?, ?> bindingMap, final Writer writer) {
        try {
            rawTemplate.process(bindingMap, writer);
        } catch (final freemarker.template.TemplateException e) {
            throw new InternalException(e);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void render(final Map<?, ?> bindingMap, final OutputStream out) {
        render(bindingMap, IoKit.toWriter(out, Charset.forName(this.rawTemplate.getEncoding())));
    }

}
