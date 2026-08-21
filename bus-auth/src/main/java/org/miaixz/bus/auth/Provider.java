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

import org.miaixz.bus.core.basic.entity.Tracer;

/**
 * Represents one protocol-neutral authentication provider group owned by a Library.
 * <p>
 * A provider belongs to exactly one {@link Library} through {@link #library_id} and groups one or more authentication
 * {@link Source Sources}. It stores only provider-level presentation, selection, and management configuration; protocol
 * selection and protocol options belong exclusively to each Source.
 * </p>
 * <p>
 * Provider has no second persistent {@code namespace_id}: its resource namespace is resolved through
 * {@code library_id -> Library.namespace_id}. The inherited Tracer {@code x_namespace_id} remains transient request
 * context and must not be used as registration ownership.
 * </p>
 * <p>
 * This mutable persistence model is intended for external projects to extend and map to their storage model. It has no
 * reverse collection, protocol identifier, protocol Discovery document, or runtime settings object.
 * </p>
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Provider extends Tracer {

    /**
     * Required non-blank identifier of the owning Library. The referenced Library must exist; when this Provider is
     * enabled, the Library must also be enabled. Multiple Providers may reference the same Library.
     */
    private String library_id;

    /**
     * Project-managed stable Provider code used only by external management interfaces.
     */
    private String code;

    /**
     * Project-managed human-readable Provider name displayed by external interfaces.
     */
    private String name;

    /**
     * Optional external media reference for the Provider icon. {@code null} or blank means no Provider-level icon; a
     * value may be a relative path, storage identifier, or HTTP(S) URL as interpreted by the external project.
     */
    private String icon;

    /**
     * Optional project-managed presentation order within the owning Library.
     */
    private Integer sort;

    /**
     * Optional JSON object encoded as text for external management extensions. {@code null} or blank means no metadata;
     * its members must not affect Source selection, protocol execution, authorization, or security decisions.
     */
    private String metadata;

    /**
     * Optional human-readable Provider description. {@code null} means that no description is supplied; the value is
     * presentation-only and must not be interpreted as authentication policy.
     */
    private String description;

    /**
     * Creates an empty persistence model for an external provider service implementation.
     */
    public Provider() {
        // No initialization required.
    }

}
