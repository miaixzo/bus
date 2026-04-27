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

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Transient;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

import lombok.Getter;
import lombok.Setter;

/**
 * MCP tool or service definition.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@SuperBuilder
public class McpAssets extends Assets {

    /**
     * MCP-specific metadata view.
     */
    @Transient
    private Meta meta;

    /**
     * Creates an MCP assets entry with type preset to {@link Type#MCP}.
     */
    public McpAssets() {
        setType(Type.MCP.key());
    }

    /**
     * Replaces the raw metadata payload and clears the parsed MCP metadata view.
     *
     * @param metadata raw metadata JSON
     */
    @Override
    public void metadata(String metadata) {
        super.metadata(metadata);
        this.meta = null;
    }

    /**
     * Returns MCP-specific metadata parsed from the raw asset metadata JSON payload.
     *
     * @return MCP metadata
     */
    public Meta meta() {
        if (meta == null) {
            meta = Meta.from(getMetadata());
        }
        return meta;
    }

    /**
     * Replaces MCP-specific metadata and writes it back to the raw asset metadata JSON payload.
     *
     * @param meta MCP metadata
     */
    public void meta(Meta meta) {
        this.meta = meta == null ? new Meta() : meta;
        super.metadata(this.meta.merge());
    }

    /**
     * Writes the current MCP metadata view back to the raw asset metadata JSON payload.
     *
     * @return this asset
     */
    public McpAssets normalizeMeta() {
        meta(meta());
        return this;
    }

    /**
     * Returns the public tool name exposed by the MCP entry.
     *
     * @return tool name
     */
    public String toolName() {
        return meta().getToolName();
    }

    /**
     * Stores the public tool name into MCP metadata.
     *
     * @param toolName tool name
     */
    public void toolName(String toolName) {
        Meta meta = meta();
        meta.setToolName(toolName);
        meta(meta);
    }

    /**
     * Returns the transport protocol used to reach the MCP server.
     *
     * @return transport name
     */
    public String transport() {
        return meta().getTransport();
    }

    /**
     * Stores the transport protocol into MCP metadata.
     *
     * @param transport transport name
     */
    public void transport(String transport) {
        Meta meta = meta();
        meta.setTransport(transport);
        meta(meta);
    }

    /**
     * Returns the input or capability schema advertised by the MCP entry.
     *
     * @return schema payload
     */
    public String schema() {
        return meta().getSchema();
    }

    /**
     * Stores the schema payload into MCP metadata.
     *
     * @param schema schema payload
     */
    public void schema(String schema) {
        Meta meta = meta();
        meta.setSchema(schema);
        meta(meta);
    }

    /**
     * Returns tags attached to the MCP entry.
     *
     * @return tags
     */
    public List<String> tags() {
        return meta().getTags();
    }

    /**
     * Stores tags into MCP metadata.
     *
     * @param tags tags
     */
    public void tags(List<String> tags) {
        Meta meta = meta();
        meta.setTags(tags == null ? null : new ArrayList<>(tags));
        meta(meta);
    }

    /**
     * MCP-specific metadata payload stored directly in the raw asset metadata JSON payload.
     */
    @Getter
    @Setter
    public static class Meta {

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
         * Creates an empty MCP metadata view.
         */
        public Meta() {
        }

        /**
         * Parses MCP metadata from a raw metadata JSON payload.
         *
         * @param metadata raw metadata JSON
         * @return parsed MCP metadata
         */
        public static Meta from(String metadata) {
            if (StringKit.isBlank(metadata)) {
                return new Meta();
            }
            try {
                Meta meta = JsonKit.toPojo(metadata, Meta.class);
                return meta == null ? new Meta() : meta;
            } catch (Exception ignore) {
                return new Meta();
            }
        }

        /**
         * Converts this MCP metadata to JSON.
         *
         * @return metadata JSON
         */
        public String merge() {
            return JsonKit.toJsonString(this);
        }

    }

}
