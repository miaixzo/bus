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
package org.miaixz.bus.extra.template;

import java.io.Writer;
import java.util.Map;

/**
 * Provides template utility methods for quick template merging and rendering.
 *
 * @author Kimi Liu
 * @since Java 21+
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
