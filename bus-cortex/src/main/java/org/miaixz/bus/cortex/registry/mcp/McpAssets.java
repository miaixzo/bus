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
package org.miaixz.bus.cortex.registry.mcp;

import java.util.List;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Species;

import lombok.Getter;
import lombok.Setter;

/**
 * MCP tool or service definition.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class McpAssets extends Assets {

    /**
     * Public tool name exposed by the MCP entry.
     */
    private String toolName;
    /**
     * Transport protocol used to reach the MCP server.
     */
    private String transport;
    /**
     * Input or capability schema advertised by the MCP entry.
     */
    private String schema;
    /**
     * Tags attached to the MCP entry for discovery.
     */
    private List<String> tags;

    /**
     * Creates an MCP assets entry with type preset to {@link Species#MCP}.
     */
    public McpAssets() {
        setSpecies(Species.MCP);
    }

}
