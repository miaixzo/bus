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

import java.io.*;
import java.util.Map;

import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Abstract template interface. This interface defines the contract for template rendering operations, allowing for
 * various template engines to be used interchangeably.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Template {

    /**
     * Renders the template with the given binding parameters and writes the result to a {@link Writer}.
     *
     * @param bindingMap A map of parameters to bind to the template variables.
     * @param writer     The {@link Writer} to which the rendered content will be written.
     */
    void render(Map<?, ?> bindingMap, Writer writer);

    /**
     * Renders the template with the given binding parameters and writes the result to an {@link OutputStream}.
     *
     * @param bindingMap A map of parameters to bind to the template variables.
     * @param out        The {@link OutputStream} to which the rendered content will be written.
     */
    void render(Map<?, ?> bindingMap, OutputStream out);

    /**
     * Renders the template with the given binding parameters and writes the result to a specified {@link File}. The
     * output stream will be automatically closed after rendering.
     *
     * @param bindingMap A map of parameters to bind to the template variables.
     * @param file       The {@link File} to which the rendered content will be written.
     */
    default void render(final Map<?, ?> bindingMap, final File file) {
        BufferedOutputStream out = null;
        try {
            out = FileKit.getOutputStream(file);
            this.render(bindingMap, out);
        } finally {
            IoKit.closeQuietly(out);
        }
    }

    /**
     * Renders the template with the given binding parameters and returns the result as a {@link String}.
     *
     * @param bindingMap A map of parameters to bind to the template variables.
     * @return The rendered content as a {@link String}.
     */
    default String render(final Map<?, ?> bindingMap) {
        final StringWriter writer = new StringWriter();
        render(bindingMap, writer);
        return writer.toString();
    }

}
