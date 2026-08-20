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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.miaixz.bus.auth.Library;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.Validator;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Validates persistence-level invariants of the authentication {@link Library} entity.
 * <p>
 * The validator checks namespace scope, the namespace-local code, required presentation fields, icon location, launch
 * target, and launch URL template syntax. It validates placeholder names but does not resolve Principal values; only
 * {@link LibraryLaunchService} performs request-scoped substitution.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LibraryValidator {

    /**
     * Maximum Library code length fixed by the management model.
     */
    private static final int MAX_CODE_LENGTH = 50;

    /**
     * Creates a stateless Library validator.
     */
    public LibraryValidator() {
        // No initialization required.
        // Validation has no mutable or external dependencies.
    }

    /**
     * Validates the namespace-local Library code without relying on a generic URL or wildcard expression.
     *
     * @param code Library code
     * @throws ValidateException if the code is blank, too long, or contains a disallowed character
     */
    private static void validateCode(final String code) {
        Validator.validateTrue(StringKit.isNotBlank(code), "Library code must not be blank");
        Validator.validateTrue(
                code.length() <= MAX_CODE_LENGTH,
                "Library code must not exceed {} characters",
                MAX_CODE_LENGTH);
        for (int index = 0; index < code.length(); index++) {
            final char value = code.charAt(index);
            final boolean allowed = value == Symbol.C_MINUS || value == Symbol.C_UNDERLINE
                    || value >= Symbol.C_ZERO && value <= Symbol.C_NINE || value >= 'A' && value <= 'Z'
                    || value >= 'a' && value <= 'z';
            Validator.validateTrue(allowed, "Library code contains a disallowed character at index {}", index);
        }
    }

    /**
     * Replaces syntactically valid claim placeholders with an RFC 3986 unreserved marker for URL validation.
     *
     * @param template persisted launch URL template
     * @return URL candidate with every placeholder replaced by {@code value}
     * @throws ValidateException if the template is blank or contains unbalanced or invalid placeholders
     */
    private static String replacePlaceholders(final String template) {
        Validator.validateTrue(StringKit.isNotBlank(template), "Library launch URL must not be blank");
        final StringBuilder candidate = new StringBuilder(template.length());
        for (int index = 0; index < template.length();) {
            final char current = template.charAt(index);
            if (current == Symbol.C_BRACE_RIGHT) {
                throw new ValidateException("Library launch URL contains an unmatched closing placeholder");
            }
            if (current != Symbol.C_BRACE_LEFT) {
                candidate.append(current);
                index++;
                continue;
            }
            final int end = template.indexOf(Symbol.C_BRACE_RIGHT, index + 1);
            if (end < 0) {
                throw new ValidateException("Library launch URL contains an unterminated placeholder");
            }
            final String claim = template.substring(index + 1, end);
            validateClaimName(claim);
            candidate.append("value");
            index = end + 1;
        }
        return candidate.toString();

    }

    /**
     * Validates a claim placeholder name using an explicit stable character set.
     *
     * @param claim placeholder claim name
     * @throws ValidateException if the name is blank or contains a character outside letters, digits, dot, dash, and
     *                           underscore
     */
    private static void validateClaimName(final String claim) {
        Validator.validateTrue(StringKit.isNotBlank(claim), "Library launch placeholder must not be blank");
        for (int index = 0; index < claim.length(); index++) {
            final char value = claim.charAt(index);
            final boolean allowed = value == Symbol.C_DOT || value == Symbol.C_MINUS || value == Symbol.C_UNDERLINE
                    || value >= Symbol.C_ZERO && value <= Symbol.C_NINE || value >= 'A' && value <= 'Z'
                    || value >= 'a' && value <= 'z';
            Validator.validateTrue(
                    allowed,
                    "Library launch placeholder contains a disallowed character at index {}",
                    index);
        }
    }

    /**
     * Validates a relative location or an absolute HTTP(S) location.
     *
     * @param value    location candidate
     * @param optional whether a blank value is accepted
     * @param field    safe field label for validation messages
     * @throws ValidateException if the location is malformed, scheme-relative, or uses a prohibited scheme
     */
    private static void validateLocation(final String value, final boolean optional, final String field) {
        if (StringKit.isBlank(value)) {
            Validator.validateTrue(optional, "Library {} must not be blank", field);
            return;
        }
        Validator.validateTrue(value.equals(value.trim()), "Library {} must not contain surrounding whitespace", field);
        Validator.validateFalse(value.startsWith("//"), "Library {} must not be scheme-relative", field);
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute()) {
                Validator.validateTrue(uri.getRawAuthority() == null, "Library {} must not define an authority", field);
                return;
            }
            final String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            Validator.validateTrue(
                    Protocol.HTTP.name.equals(scheme) || Protocol.HTTPS.name.equals(scheme),
                    "Library {} must use HTTP or HTTPS",
                    field);
            UnoUrl.parse(value);
        } catch (URISyntaxException | RuntimeException cause) {
            if (cause instanceof ValidateException validateException) {
                throw validateException;
            }
            throw new ValidateException("Library " + field + " is not a valid relative or HTTP(S) location", cause);
        }
    }

    /**
     * Validates one complete Library persistence entity without mutating it.
     *
     * @param library complete Library entity supplied by an external management or loading boundary
     * @throws ValidateException if a required field or safe URL invariant is invalid
     */
    public void validate(final Library library) {
        Assert.notNull(library, "Library must not be null");
        Validator.validateTrue(
                StringKit.isNotBlank(library.getNamespace_id()),
                "Library namespace id must not be blank");
        validateCode(library.getCode());
        Validator.validateTrue(StringKit.isNotBlank(library.getName()), "Library name must not be blank");
        Assert.notNull(library.getTarget(), "Library launch target must not be null");
        Validator.validateTrue(library.targetValue() != null, "Library launch target is unsupported");
        validateLocation(library.getIcon(), true, "icon");
        final String launchCandidate = replacePlaceholders(library.getUrl());
        validateLocation(launchCandidate, false, "launch URL");
    }

}
