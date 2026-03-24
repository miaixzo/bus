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

import java.io.Serial;
import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the documentation generation engine.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Builder
public class EngineConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Whether to automatically open the output directory after generation is complete.
     */
    private boolean openOutputDir;
    /**
     * The directory where the generated file will be saved.
     */
    private String fileOutputDir;
    /**
     * The type of file to generate (e.g., HTML, WORD, MD).
     */
    private EngineFileType fileType;
    /**
     * The template engine to use for generation.
     */
    private TemplateType produceType;
    /**
     * The path to a custom template file. The template must be compatible with the chosen file type and template engine
     * syntax, otherwise, the generation may fail or produce incorrect output.
     */
    private String customTemplate;
    /**
     * The name of the generated file.
     */
    private String fileName;

}
