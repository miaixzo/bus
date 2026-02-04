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
