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
