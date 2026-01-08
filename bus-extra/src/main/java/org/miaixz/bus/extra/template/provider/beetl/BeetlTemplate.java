/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.io.Serial;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

import org.miaixz.bus.extra.template.Template;

/**
 * Beetl template implementation. This class wraps a Beetl {@link org.beetl.core.Template} object, providing a unified
 * interface for rendering templates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeetlTemplate implements Template, Serializable {

    @Serial
    private static final long serialVersionUID = 2852288593976L;

    /**
     * The raw Beetl template object.
     */
    private final org.beetl.core.Template rawTemplate;

    /**
     * Constructs a new {@code BeetlTemplate} instance.
     *
     * @param beetlTemplate The raw Beetl template object to be wrapped. Must not be {@code null}.
     */
    public BeetlTemplate(final org.beetl.core.Template beetlTemplate) {
        this.rawTemplate = beetlTemplate;
    }

    /**
     * Wraps a Beetl template object into a {@code BeetlTemplate} instance.
     *
     * @param beetlTemplate The raw Beetl template object ({@link org.beetl.core.Template}).
     * @return A new {@code BeetlTemplate} instance, or {@code null} if the input {@code beetlTemplate} is {@code null}.
     */
    public static BeetlTemplate wrap(final org.beetl.core.Template beetlTemplate) {
        return (null == beetlTemplate) ? null : new BeetlTemplate(beetlTemplate);
    }

    /**
     * Renders the template with the given data model to a writer. This method is designed to be overridden by
     * subclasses for custom rendering logic.
     *
     * Subclasses may override to add custom binding or rendering logic.
     *
     * @param bindingMap The data model to bind to the template.
     * @param writer     The writer to output the rendered template to.
     */
    @Override
    public void render(final Map<?, ?> bindingMap, final Writer writer) {
        rawTemplate.binding(bindingMap);
        rawTemplate.renderTo(writer);
    }

    /**
     * Renders the template with the given data model to an output stream. This method is designed to be overridden by
     * subclasses for custom rendering logic.
     *
     * Subclasses may override to add custom binding or rendering logic.
     *
     * @param bindingMap The data model to bind to the template.
     * @param out        The output stream to write the rendered template to.
     */
    @Override
    public void render(final Map<?, ?> bindingMap, final OutputStream out) {
        rawTemplate.binding(bindingMap);
        rawTemplate.renderTo(out);
    }

}
