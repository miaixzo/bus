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
package org.miaixz.bus.extra.template.provider.freemarker;

import java.io.Reader;
import java.io.StringReader;

import freemarker.cache.TemplateLoader;

/**
 * A {@link TemplateLoader} implementation that loads templates directly from a string. This is useful for situations
 * where the template content is provided as a string rather than from a file or other resource.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleStringTemplateLoader implements TemplateLoader {

    /**
     * Finds the template source. This method is designed to be overridden by subclasses for custom template lookup.
     *
     * Subclasses may override to add caching or custom loading logic.
     *
     * @param name The name of the template to find.
     * @return The template source object.
     */
    @Override
    public Object findTemplateSource(final String name) {
        return name;
    }

    /**
     * Gets the last modified time of the template source. This method is designed to be overridden by subclasses for
     * custom timestamp tracking.
     *
     * Subclasses may override to provide actual file modification times.
     *
     * @param templateSource The template source object.
     * @return The last modified timestamp.
     */
    @Override
    public long getLastModified(final Object templateSource) {
        return System.currentTimeMillis();
    }

    /**
     * Gets a reader for the template source. This method is designed to be overridden by subclasses for custom reader
     * creation.
     *
     * Subclasses may override to add custom encoding or buffering.
     *
     * @param templateSource The template source object.
     * @param encoding       The character encoding (not used in this implementation).
     * @return A reader for the template content.
     */
    @Override
    public Reader getReader(final Object templateSource, final String encoding) {
        return new StringReader((String) templateSource);
    }

    @Override
    public void closeTemplateSource(final Object templateSource) {
        // ignore
    }

}
