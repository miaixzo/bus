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
package org.miaixz.bus.cortex.registry.prompt;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Species;

import lombok.Getter;
import lombok.Setter;

/**
 * Prompt template definition.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class PromptAssets extends Assets {

    /**
     * Prompt name exposed for lookup and invocation.
     */
    private String name;
    /**
     * Prompt template content.
     */
    private String template;
    /**
     * Declared template variables accepted by the prompt.
     */
    private List<String> variables;
    /**
     * Tags attached to the prompt for discovery.
     */
    private List<String> tags;
    /**
     * Human-readable description of the prompt.
     */
    private String description;
    /**
     * Additional labels associated with the prompt.
     */
    private Map<String, String> labels;

    /**
     * Creates a prompt assets entry with type preset to {@link Species#PROMPT}.
     */
    public PromptAssets() {
        setSpecies(Species.PROMPT);
    }

}
