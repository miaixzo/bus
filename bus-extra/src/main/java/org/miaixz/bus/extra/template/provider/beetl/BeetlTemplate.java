/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.template.provider.beetl;

import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

import org.miaixz.bus.extra.template.Template;

/**
 * Beetl模板实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeetlTemplate implements Template, Serializable {

    private static final long serialVersionUID = -1L;

    private final org.beetl.core.Template rawTemplate;

    /**
     * 构造
     *
     * @param beetlTemplate Beetl的模板对象 {@link org.beetl.core.Template}
     */
    public BeetlTemplate(final org.beetl.core.Template beetlTemplate) {
        this.rawTemplate = beetlTemplate;
    }

    /**
     * 包装Beetl模板
     *
     * @param beetlTemplate Beetl的模板对象 {@link org.beetl.core.Template}
     * @return BeetlTemplate
     */
    public static BeetlTemplate wrap(final org.beetl.core.Template beetlTemplate) {
        return (null == beetlTemplate) ? null : new BeetlTemplate(beetlTemplate);
    }

    @Override
    public void render(final Map<?, ?> bindingMap, final Writer writer) {
        rawTemplate.binding(bindingMap);
        rawTemplate.renderTo(writer);
    }

    @Override
    public void render(final Map<?, ?> bindingMap, final OutputStream out) {
        rawTemplate.binding(bindingMap);
        rawTemplate.renderTo(out);
    }

}
