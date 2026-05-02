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

import org.miaixz.bus.core.Loader;
import org.miaixz.bus.core.lang.loader.LazyFunLoader;

/**
 * Utility class for {@link javax.xml.parsers.SAXParserFactory}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SAXParserFactory {

    /**
     * Constructs a new SAX parser factory utility.
     */
    public SAXParserFactory() {

    }

    /**
     * Cache for the SAX parser factory.
     */
    private static final Loader<javax.xml.parsers.SAXParserFactory> factory = LazyFunLoader
            .of(() -> createFactory(false, true));

    /**
     * Gets the global {@link javax.xml.parsers.SAXParserFactory} instance.
     * <ul>
     * <li>Default: not validating.</li>
     * <li>Default: namespace support enabled.</li>
     * </ul>
     *
     * @return The global {@link javax.xml.parsers.SAXParserFactory} instance.
     */
    public static javax.xml.parsers.SAXParserFactory getFactory() {
        return factory.get();
    }

    /**
     * Creates a new {@link javax.xml.parsers.SAXParserFactory} instance.
     *
     * @param validating     {@code true} to enable validation, {@code false} otherwise.
     * @param namespaceAware {@code true} to enable namespace awareness, {@code false} otherwise.
     * @return A new {@link javax.xml.parsers.SAXParserFactory} instance.
     */
    public static javax.xml.parsers.SAXParserFactory createFactory(
            final boolean validating,
            final boolean namespaceAware) {
        final javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(validating);
        factory.setNamespaceAware(namespaceAware);

        return XXE.disableXXE(factory);
    }

}
