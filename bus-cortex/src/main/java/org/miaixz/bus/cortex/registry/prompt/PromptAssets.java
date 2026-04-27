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
 * Prompt template definition.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@SuperBuilder
public class PromptAssets extends Assets {

    /**
     * Prompt-specific metadata view.
     */
    @Transient
    private Meta meta;

    /**
     * Creates a prompt assets entry with type preset to {@link Type#PROMPT}.
     */
    public PromptAssets() {
        setType(Type.PROMPT.key());
    }

    /**
     * Replaces the raw metadata payload and clears the parsed prompt metadata view.
     *
     * @param metadata raw metadata JSON
     */
    @Override
    public void metadata(String metadata) {
        super.metadata(metadata);
        this.meta = null;
    }

    /**
     * Returns prompt-specific metadata parsed from the raw asset metadata JSON payload.
     *
     * @return prompt metadata
     */
    public Meta meta() {
        if (meta == null) {
            meta = Meta.from(getMetadata());
        }
        return meta;
    }

    /**
     * Replaces prompt-specific metadata and writes it back to the raw asset metadata JSON payload.
     *
     * @param meta prompt metadata
     */
    public void meta(Meta meta) {
        this.meta = meta == null ? new Meta() : meta;
        super.metadata(this.meta.merge());
    }

    /**
     * Writes the current prompt metadata view back to the raw asset metadata JSON payload.
     *
     * @return this asset
     */
    public PromptAssets normalizeMeta() {
        meta(meta());
        return this;
    }

    /**
     * Returns prompt template content.
     *
     * @return template content
     */
    public String template() {
        return meta().getTemplate();
    }

    /**
     * Stores prompt template content into prompt metadata.
     *
     * @param template template content
     */
    public void template(String template) {
        Meta meta = meta();
        meta.setTemplate(template);
        meta(meta);
    }

    /**
     * Returns declared template variables accepted by the prompt.
     *
     * @return variables
     */
    public List<String> variables() {
        return meta().getVariables();
    }

    /**
     * Stores declared template variables into prompt metadata.
     *
     * @param variables variables
     */
    public void variables(List<String> variables) {
        Meta meta = meta();
        meta.setVariables(variables == null ? null : new ArrayList<>(variables));
        meta(meta);
    }

    /**
     * Returns tags attached to the prompt.
     *
     * @return tags
     */
    public List<String> tags() {
        return meta().getTags();
    }

    /**
     * Stores tags into prompt metadata.
     *
     * @param tags tags
     */
    public void tags(List<String> tags) {
        Meta meta = meta();
        meta.setTags(tags == null ? null : new ArrayList<>(tags));
        meta(meta);
    }

    /**
     * Prompt-specific metadata payload stored directly in the raw asset metadata JSON payload.
     */
    @Getter
    @Setter
    public static class Meta {

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
         * Creates an empty prompt metadata view.
         */
        public Meta() {
        }

        /**
         * Parses prompt metadata from a raw metadata JSON payload.
         *
         * @param metadata raw metadata JSON
         * @return parsed prompt metadata
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
         * Converts this prompt metadata to JSON.
         *
         * @return metadata JSON
         */
        public String merge() {
            return JsonKit.toJsonString(this);
        }

    }

}
