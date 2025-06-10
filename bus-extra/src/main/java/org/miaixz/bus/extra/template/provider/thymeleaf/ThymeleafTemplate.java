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
package org.miaixz.bus.extra.template.provider.thymeleaf;

import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.extra.template.Template;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Thymeleaf模板实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThymeleafTemplate implements Template, Serializable {

    @Serial
    private static final long serialVersionUID = 2852289111659L;

    private final TemplateEngine engine;
    private final String template;
    private final java.nio.charset.Charset charset;

    /**
     * 构造
     *
     * @param engine   Thymeleaf的模板对象 {@link TemplateEngine}
     * @param template 模板路径或模板内容
     * @param charset  编码
     */
    public ThymeleafTemplate(final TemplateEngine engine, final String template,
            final java.nio.charset.Charset charset) {
        this.engine = engine;
        this.template = template;
        this.charset = ObjectKit.defaultIfNull(charset, Charset.UTF_8);
    }

    /**
     * 包装Thymeleaf模板
     *
     * @param engine   Thymeleaf的模板引擎对象 {@link TemplateEngine}
     * @param template 模板路径或模板内容
     * @param charset  编码
     * @return {@code ThymeleafTemplate}
     */
    public static ThymeleafTemplate wrap(final TemplateEngine engine, final String template,
            final java.nio.charset.Charset charset) {
        return (null == engine) ? null : new ThymeleafTemplate(engine, template, charset);
    }

    @Override
    public void render(final Map<?, ?> bindingMap, final Writer writer) {
        final Map<String, Object> map = Convert.convert(new TypeReference<>() {
        }, bindingMap);
        final Context context = new Context(Locale.getDefault(), map);
        this.engine.process(this.template, context, writer);
    }

    @Override
    public void render(final Map<?, ?> bindingMap, final OutputStream out) {
        render(bindingMap, IoKit.toWriter(out, this.charset));
    }

}
