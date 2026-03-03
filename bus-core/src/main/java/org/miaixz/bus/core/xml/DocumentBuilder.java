/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
     * Constructs a new DocumentBuilder. Utility class constructor for static access.
     */
    private DocumentBuilder() {
    }

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
