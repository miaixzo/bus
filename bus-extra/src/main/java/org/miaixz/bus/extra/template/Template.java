/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
