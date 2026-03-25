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
 * @since Java 21+
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
