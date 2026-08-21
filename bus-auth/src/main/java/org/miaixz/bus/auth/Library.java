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
package org.miaixz.bus.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Namespace;
import org.miaixz.bus.core.lang.Enumers;

/**
 * Represents the single authentication application displayed by administrative and end-user application hubs.
 * <p>
 * The inherited {@link Namespace} contract provides the single persistent namespace scope together with identity and
 * audit metadata. This entity adds only authentication application presentation and launch metadata. A {@code Library}
 * does not own collections of providers or sources; those resources reference it through their direct identifiers.
 * </p>
 * <p>
 * Instances are mutable persistence models intended for external projects to extend and map to their storage model.
 * Request-scoped launch URL resolution belongs to the integrating project and is never stored here.
 * </p>
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Library extends Namespace {

    /**
     * Project-managed namespace-local application code. Format and uniqueness policies belong to the external
     * management implementation and do not affect authentication compilation.
     */
    private String code;
    /**
     * Project-managed human-readable application name displayed in external application hubs.
     */
    private String name;
    /**
     * Optional project-managed application icon location, never interpreted by authentication execution.
     */
    private String icon;
    /**
     * Project-managed launch template interpreted and validated only by the integrating project.
     */
    private String url;
    /**
     * Project-managed browser browsing-context code. Built-in values map to {@link Target}, while project validation
     * decides whether a missing or unsupported value is accepted.
     */
    private Integer target;
    /**
     * Optional project-managed presentation order used only by external application hubs.
     */
    private Integer sort;
    /**
     * Optional external presentation category. {@code null} means that the application is unclassified; a non-blank
     * value is an external grouping key and must never change authentication or authorization behavior.
     */
    private String category;
    /**
     * Optional JSON object encoded as text for provider-neutral presentation and management extensions. {@code null} or
     * blank means no extensions. Its members must not affect protocol execution, authorization, or security decisions.
     */
    private String metadata;
    /**
     * Optional human-readable publisher or application owner name. {@code null} means unknown or undisclosed; the value
     * is displayed only and is not a verified security identity.
     */
    private String publisher;
    /**
     * Optional human-readable application description. {@code null} means that no description is supplied; the value is
     * presentation-only and is not interpreted as policy or protocol configuration.
     */
    private String description;

    /**
     * Creates an empty persistence model for external storage and mapping implementations.
     */
    public Library() {
        // No initialization required.
    }

    /**
     * Returns the typed browser launch target represented by the persisted numeric code.
     *
     * @return browser launch target, or {@code null} when no supported target is configured
     */
    public Target targetValue() {
        if (target == null) {
            return null;
        }
        for (Target value : Target.values()) {
            if (value.code() == target.intValue()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Stores the stable numeric code of a typed browser launch target.
     *
     * @param value browser launch target
     */
    public void targetValue(final Target value) {
        this.target = value == null ? null : value.code();
    }

    /**
     * Defines the browser browsing context used for application launch links.
     *
     * @author Kimi Liu
     */
    public enum Target implements Enumers<Target> {

        /**
         * Opens the application in the current browsing context.
         */
        SELF(1),

        /**
         * Opens the application in a new browsing context.
         */
        BLANK(2);

        /**
         * Stable persistence code independent of enum declaration order.
         */
        private final int code;

        /**
         * Creates a launch target with its stable persistence code.
         *
         * @param code stable persistence code
         */
        Target(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persistence code for this launch target.
         *
         * @return stable target code
         */
        @Override
        public int code() {
            return code;
        }

    }

}
