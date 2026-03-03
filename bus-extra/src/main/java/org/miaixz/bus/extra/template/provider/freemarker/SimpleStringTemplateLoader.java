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
