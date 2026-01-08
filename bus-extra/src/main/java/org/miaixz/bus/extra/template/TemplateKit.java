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
package org.miaixz.bus.extra.template;

import java.io.Writer;
import java.util.Map;

/**
 * Provides template utility methods for quick template merging and rendering.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TemplateKit {

    /**
     * Retrieves a singleton instance of the template engine.
     *
     * @return The singleton {@link TemplateProvider} instance.
     */
    public static TemplateProvider getEngine() {
        return TemplateFactory.get();
    }

    /**
     * Renders the given template content with the provided binding parameters and returns the result as a string.
     *
     * @param templateContent The template content to be rendered.
     * @param bindingMap      A map of parameters to bind to the template variables.
     * @return The rendered content as a {@link String}.
     */
    public static String render(final String templateContent, final Map<?, ?> bindingMap) {
        return getEngine().getTemplate(templateContent).render(bindingMap);
    }

    /**
     * Renders the given template content with the provided binding parameters and writes the result to a
     * {@link Writer}.
     *
     * @param templateContent The template content to be rendered.
     * @param bindingMap      A map of parameters to bind to the template variables.
     * @param writer          The {@link Writer} to which the rendered content will be written.
     */
    public static void render(final String templateContent, final Map<?, ?> bindingMap, final Writer writer) {
        getEngine().getTemplate(templateContent).render(bindingMap, writer);
    }

}
