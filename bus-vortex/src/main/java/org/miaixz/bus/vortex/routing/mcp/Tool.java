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
package org.miaixz.bus.vortex.routing.mcp;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A data model representing a single tool provided by an MCP (Model Context Protocol) service.
 * <p>
 * A "tool" is a function or capability that an AI model can invoke. This class defines the standard structure for
 * describing such a tool, including its name, a human-readable description, and a schema for its input parameters.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {

    /**
     * The unique name of the tool (e.g., "getCurrentWeather", "sendEmail"). This name is used to invoke the tool.
     */
    private String name;

    /**
     * A detailed, human-readable description of what the tool does, what its parameters are, and what it returns. This
     * description is crucial for AI models to understand when and how to use the tool.
     */
    private String description;

    /**
     * The schema for the tool's input parameters, typically represented as a JSON Schema object. This map defines the
     * expected arguments, their types, and whether they are required.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * {
     *   "type": "object",
     *   "properties": {
     *     "location": {
     *       "type": "string",
     *       "description": "The city and state, e.g., San Francisco, CA"
     *     },
     *     "unit": {
     *       "type": "string",
     *       "enum": ["celsius", "fahrenheit"]
     *     }
     *   },
     *   "required": ["location"]
     * }
     * }</pre>
     */
    private Map<String, Object> inputSchema;

}
