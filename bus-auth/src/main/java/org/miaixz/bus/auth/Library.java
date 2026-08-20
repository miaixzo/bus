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
 * The inherited {@link Namespace} fields provide identity, lifecycle, audit, query, caller, trace, and namespace scope.
 * This entity adds only authentication application presentation and launch metadata. A {@code Library} does not own
 * collections of providers or sources; those resources reference it through their direct identifiers.
 * </p>
 * <p>
 * Instances are mutable persistence models intended for external projects to extend and map to their storage model.
 * Request-scoped launch URL resolution is returned directly by the Library launch service and is never stored here.
 * </p>
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Library extends Namespace {

    /**
     * Required namespace-local stable application code. The value must be non-blank, at most 50 characters, contain
     * only ASCII letters, digits, {@code -}, or {@code _}, and be unique within {@link #getNamespace_id()}.
     */
    private String code;
    /**
     * Required non-blank human-readable application name displayed in management and end-user application hubs.
     */
    private String name;
    /**
     * Optional application icon location. {@code null} or blank means no icon; otherwise the value must be a relative
     * location or an absolute HTTP(S) URL, must not be scheme-relative, and must not contain surrounding whitespace.
     */
    private String icon;
    /**
     * Required non-blank launch template. The value must be a relative location or absolute HTTP(S) URL and may contain
     * claim placeholders such as {@code {sub}} whose names use only letters, digits, dot, dash, or underscore. Allowed
     * placeholders are resolved and percent-encoded by {@link org.miaixz.bus.auth.library.LibraryLaunchService}.
     */
    private String url;
    /**
     * Required persisted browser browsing-context code. {@code 1} means {@link Target#SELF}; {@code 2} means
     * {@link Target#BLANK}. {@code null} and every other integer are unsupported.
     */
    private Integer target;
    /**
     * Optional signed presentation order used by external application hubs. Lower values are displayed first;
     * {@code null} means that no explicit order is configured and the external project chooses the fallback order.
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
