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
package org.miaixz.bus.extra.template.provider.thymeleaf;

import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.extra.template.Template;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Thymeleaf template implementation. This class wraps a Thymeleaf {@link TemplateEngine} object, providing a unified
 * interface for rendering templates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThymeleafTemplate implements Template, Serializable {

    @Serial
    private static final long serialVersionUID = 2852289111659L;

    /**
     * The raw Thymeleaf template engine object.
     */
    private final TemplateEngine engine;
    /**
     * The template path or content.
     */
    private final String template;
    /**
     * The character set for the template.
     */
    private final java.nio.charset.Charset charset;

    /**
     * Constructs a new {@code ThymeleafTemplate} instance.
     *
     * @param engine   The Thymeleaf template engine object ({@link TemplateEngine}).
     * @param template The template path or template content.
     * @param charset  The character set for the template. If {@code null}, UTF-8 will be used.
     */
    public ThymeleafTemplate(final TemplateEngine engine, final String template,
            final java.nio.charset.Charset charset) {
        this.engine = engine;
        this.template = template;
        this.charset = ObjectKit.defaultIfNull(charset, Charset.UTF_8);
    }

    /**
     * Wraps a Thymeleaf template engine and template into a {@code ThymeleafTemplate} instance.
     *
     * @param engine   The Thymeleaf template engine object ({@link TemplateEngine}).
     * @param template The template path or template content.
     * @param charset  The character set for the template.
     * @return A new {@code ThymeleafTemplate} instance, or {@code null} if the input {@code engine} is {@code null}.
     */
    public static ThymeleafTemplate wrap(
            final TemplateEngine engine,
            final String template,
            final java.nio.charset.Charset charset) {
        return (null == engine) ? null : new ThymeleafTemplate(engine, template, charset);
    }

    /**
     * Renders the template with the given data model to a writer. This method is designed to be overridden by
     * subclasses for custom rendering logic.
     *
     * and processes the template. Subclasses may override to add custom processing.
     *
     * @param bindingMap The data model to bind to the template.
     * @param writer     The writer to output the rendered template to.
     */
    @Override
    public void render(final Map<?, ?> bindingMap, final Writer writer) {
        final Map<String, Object> map = Convert.convert(new TypeReference<>() {
        }, bindingMap);
        final Context context = new Context(Locale.getDefault(), map);
        this.engine.process(this.template, context, writer);
    }

    /**
     * Renders the template with the given data model to an output stream. This method is designed to be overridden by
     * subclasses for custom rendering logic.
     *
     * using the template's charset and delegates to the render method. Subclasses may override to add custom stream
     * handling.
     *
     * @param bindingMap The data model to bind to the template.
     * @param out        The output stream to write the rendered template to.
     */
    @Override
    public void render(final Map<?, ?> bindingMap, final OutputStream out) {
        render(bindingMap, IoKit.toWriter(out, this.charset));
    }

}
