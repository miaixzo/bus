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
package org.miaixz.bus.auth.library;

import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.auth.Library;
import org.miaixz.bus.auth.Principal;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Resolves a validated Library launch template with an explicit allow-list of authenticated Principal claims.
 * <p>
 * Every claim value is encoded as one RFC 3986 URI component before substitution. The service returns the resolved URL
 * directly, never mutates the supplied Library, performs an HTTP redirect, or changes persisted launch metadata.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LibraryLaunchService {

    /**
     * Immutable claim names that may be referenced by Library launch templates.
     */
    private final Set<String> allowedClaims;

    /**
     * Creates a launch service with the complete claim placeholder allow-list.
     *
     * @param allowedClaims claim names external management may use in launch templates
     * @throws IllegalArgumentException if the set, a claim name, or a claim name value is {@code null} or blank
     */
    public LibraryLaunchService(final Set<String> allowedClaims) {
        Assert.notNull(allowedClaims, "Allowed Library launch claims must not be null");
        final Set<String> copy = new HashSet<>(allowedClaims.size());
        for (String claim : allowedClaims) {
            copy.add(Assert.notBlank(claim, "Allowed Library launch claim must not be blank"));
        }
        this.allowedClaims = Set.copyOf(copy);
    }

    /**
     * Converts a safe provider-neutral scalar claim to its lexical URI-component input.
     *
     * @param value verified claim value
     * @param claim safe claim name used in validation messages
     * @return scalar lexical value before percent encoding
     * @throws ValidateException if the claim is null, an object, or an array
     */
    private static String scalar(final JsonValue value, final String claim) {
        if (value instanceof JsonValue.StringValue stringValue) {
            return stringValue.value();
        }
        if (value instanceof JsonValue.NumberValue numberValue) {
            return numberValue.value().toPlainString();
        }
        if (value instanceof JsonValue.BooleanValue booleanValue) {
            return Boolean.toString(booleanValue.value());
        }
        throw new ValidateException("Library launch claim must be a non-null scalar: " + claim);
    }

    /**
     * Resolves allowed claim placeholders without mutating the supplied Library.
     *
     * @param library   request-scoped Library instance whose persisted URL is a validated template
     * @param principal authenticated principal providing verified claims
     * @return resolved relative or absolute HTTP(S) launch URL
     * @throws ValidateException        if a placeholder is not allowed, a claim is missing, or a claim is not a scalar
     *                                  value
     * @throws IllegalArgumentException if the Library or Principal is {@code null}
     */
    public String launch(final Library library, final Principal principal) {
        Assert.notNull(library, "Library must not be null");
        Assert.notNull(principal, "Principal must not be null");
        new LibraryValidator().validate(library);
        final String template = library.getUrl();
        final StringBuilder resolved = new StringBuilder(template.length());
        for (int index = 0; index < template.length();) {
            final char current = template.charAt(index);
            if (current != Symbol.C_BRACE_LEFT) {
                resolved.append(current);
                index++;
                continue;
            }
            final int end = template.indexOf(Symbol.C_BRACE_RIGHT, index + 1);
            final String claim = template.substring(index + 1, end);
            if (!allowedClaims.contains(claim)) {
                throw new ValidateException("Library launch claim is not allowed: " + claim);
            }
            final JsonValue value = principal.claims().values().get(claim);
            if (value == null) {
                throw new ValidateException("Library launch claim is unavailable: " + claim);
            }
            resolved.append(UrlEncoder.encodeComponent(scalar(value, claim)));
            index = end + 1;
        }
        return resolved.toString();
    }

}
