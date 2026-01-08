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
package org.miaixz.bus.shade.screw.engine;

import java.io.*;
import java.util.Locale;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.metadata.DataSchema;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * A {@link TemplateEngine} implementation that uses the Freemarker template engine. This class is responsible for
 * processing data models with Freemarker templates to generate documentation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FreemarkerEngine extends AbstractEngine {

    /**
     * The Freemarker configuration instance.
     */
    private final Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

    /**
     * Initializes the Freemarker configuration. This block sets up the template loader based on whether a custom
     * template path is provided. It also configures the default encoding and locale.
     */
    {
        try {
            String path = getEngineConfig().getCustomTemplate();
            // If a custom template path is provided and the file exists
            if (StringKit.isNotBlank(path) && FileKit.exists(path)) {
                // Get the parent directory of the custom template
                String parent = Objects.requireNonNull(FileKit.file(path)).getParent();
                // Set the template loading directory
                configuration.setDirectoryForTemplateLoading(new File(parent));
            }
            // Otherwise, load the built-in templates from the classpath
            else {
                // Set the template loader to load from the classpath
                configuration.setTemplateLoader(
                        new ClassTemplateLoader(this.getClass(), TemplateType.FREEMARKER.getTemplateDir()));
            }
            // Set the default encoding
            configuration.setDefaultEncoding(Charset.DEFAULT_UTF_8);
            // Set the locale for internationalization
            configuration.setLocale(new Locale(Builder.DEFAULT_LOCALE));
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * Constructs a new {@code FreemarkerEngine} with the specified engine configuration.
     *
     * @param templateConfig The {@link EngineConfig} for this engine.
     */
    public FreemarkerEngine(EngineConfig templateConfig) {
        super(templateConfig);
    }

    /**
     * Generates the documentation by processing the data model with a Freemarker template.
     *
     * @param info    The {@link DataSchema} data model to be processed.
     * @param docName The base name for the output document.
     * @throws InternalException if an error occurs during template processing or file I/O.
     */
    @Override
    public void produce(DataSchema info, String docName) throws InternalException {
        Assert.notNull(info, "DataModel can not be empty!");
        String path = getEngineConfig().getCustomTemplate();
        try {
            Template template;
            // If a custom template path is provided and the file exists
            if (StringKit.isNotBlank(path) && FileKit.exists(path)) {
                // Get the template from the custom file
                String fileName = new File(path).getName();
                template = configuration.getTemplate(fileName);
            }
            // Otherwise, get the default template from the system
            else {
                template = configuration.getTemplate(
                        getEngineConfig().getFileType().getTemplateNamePrefix() + TemplateType.FREEMARKER.getSuffix());
            }
            // Create the output file
            File file = getFile(docName);
            // Write the processed template to the file
            try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.UTF_8))) {
                // Process the template with the data model
                template.process(info, out);
                // Open the output directory if configured to do so
                openOutputDir();
            }
        } catch (IOException | TemplateException e) {
            throw new InternalException(e);
        }
    }

}
