/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.screw.execute;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.shade.screw.Config;
import org.miaixz.bus.shade.screw.engine.EngineFactory;
import org.miaixz.bus.shade.screw.engine.TemplateEngine;
import org.miaixz.bus.shade.screw.metadata.DataSchema;
import org.miaixz.bus.shade.screw.process.DataModelProcess;

/**
 * Executes the document generation process. This class orchestrates the steps of processing the data model and then
 * producing the final document.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProduceExecute extends AbstractExecute {

    /**
     * Constructs a {@code ProduceExecute} with the given configuration.
     *
     * @param config The {@link Config} object for the execution task.
     */
    public ProduceExecute(Config config) {
        super(config);
    }

    /**
     * Executes the document generation process. This involves processing the database schema to create a data model,
     * then using a template engine to generate the document from that model. The time taken for the process is logged.
     *
     * @throws InternalException if any error occurs during the process.
     */
    @Override
    public void execute() {
        try {
            long start = System.currentTimeMillis();
            // Process the data model from the database schema.
            DataSchema dataModel = new DataModelProcess(config).process();
            // Get a new template engine instance from the factory.
            TemplateEngine produce = new EngineFactory(config.getEngineConfig()).newInstance();
            // Generate the document.
            produce.produce(dataModel, getDocName(dataModel.getDatabase()));
            Logger.debug(
                    "database document generation complete time consuming:{}ms",
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

}
