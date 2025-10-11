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
package org.miaixz.bus.core.xml;

import javax.xml.parsers.DocumentBuilderFactory;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Utility class for {@link javax.xml.parsers.DocumentBuilder}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DocumentBuilder {

    /**
     * Creates a new {@link javax.xml.parsers.DocumentBuilder} instance.
     *
     * @param namespaceAware {@code true} to enable namespace awareness, {@code false} otherwise.
     * @return A new {@link javax.xml.parsers.DocumentBuilder} instance.
     * @throws InternalException if an error occurs during DocumentBuilder creation.
     */
    public static javax.xml.parsers.DocumentBuilder createDocumentBuilder(final boolean namespaceAware) {
        final javax.xml.parsers.DocumentBuilder builder;
        try {
            builder = createDocumentBuilderFactory(namespaceAware).newDocumentBuilder();
        } catch (final Exception e) {
            throw new InternalException(e, "Create xml document error!");
        }
        return builder;
    }

    /**
     * Creates a new {@link DocumentBuilderFactory} instance.
     * <p>
     * By default, it uses "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl".
     *
     * @param namespaceAware {@code true} to enable namespace awareness, {@code false} otherwise.
     * @return A new {@link DocumentBuilderFactory} instance.
     */
    public static DocumentBuilderFactory createDocumentBuilderFactory(final boolean namespaceAware) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Enable NamespaceAware by default, so getElementsByTagNameNS can use namespaces
        factory.setNamespaceAware(namespaceAware);
        return XXE.disableXXE(factory);
    }

}
