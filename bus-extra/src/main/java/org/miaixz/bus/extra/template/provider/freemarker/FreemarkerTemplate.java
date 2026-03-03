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

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.template.Template;

/**
 * Freemarker template implementation. This class wraps a FreeMarker {@link freemarker.template.Template} object,
 * providing a unified interface for rendering templates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FreemarkerTemplate implements Template, Serializable {

    @Serial
    private static final long serialVersionUID = 2852288838528L;

    /**
     * The raw FreeMarker template object.
     */
    freemarker.template.Template rawTemplate;

    /**
     * Constructs a new {@code FreemarkerTemplate} instance.
     *
     * @param freemarkerTemplate The raw FreeMarker template object to be wrapped. Must not be {@code null}.
     */
    public FreemarkerTemplate(final freemarker.template.Template freemarkerTemplate) {
        this.rawTemplate = freemarkerTemplate;
    }

    /**
     * Wraps a FreeMarker template object into a {@code FreemarkerTemplate} instance.
     *
     * @param freemarkerTemplate The raw FreeMarker template object ({@link freemarker.template.Template}).
     * @return A new {@code FreemarkerTemplate} instance, or {@code null} if the input {@code freemarkerTemplate} is
     *         {@code null}.
     */
    public static FreemarkerTemplate wrap(final freemarker.template.Template freemarkerTemplate) {
        return (null == freemarkerTemplate) ? null : new FreemarkerTemplate(freemarkerTemplate);
    }

    /**
     * Renders the template with the given data model to a writer. This method is designed to be overridden by
     * subclasses for custom rendering logic.
     *
     * Subclasses may override to add pre/post-processing or error handling.
     *
     * @param bindingMap The data model to bind to the template.
     * @param writer     The writer to output the rendered template to.
     * @throws InternalException if a template exception or I/O error occurs.
     */
    @Override
    public void render(final Map<?, ?> bindingMap, final Writer writer) {
        try {
            rawTemplate.process(bindingMap, writer);
        } catch (final freemarker.template.TemplateException e) {
            throw new InternalException(e);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Renders the template with the given data model to an output stream. This method is designed to be overridden by
     * subclasses for custom rendering logic.
     *
     * using the template's encoding and delegates to the render method. Subclasses may override to add custom stream
     * handling.
     *
     * @param bindingMap The data model to bind to the template.
     * @param out        The output stream to write the rendered template to.
     */
    @Override
    public void render(final Map<?, ?> bindingMap, final OutputStream out) {
        render(bindingMap, IoKit.toWriter(out, Charset.forName(this.rawTemplate.getEncoding())));
    }

}
