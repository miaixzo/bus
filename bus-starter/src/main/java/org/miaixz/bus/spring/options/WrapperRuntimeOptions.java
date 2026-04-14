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
package org.miaixz.bus.spring.options;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.net.HTTP;

import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Runtime compatibility options shared by wrapper-related HTTP components.
 * <p>
 * This class is the runtime snapshot consumed by the wrapper pipeline after Spring configuration has been bound from
 * {@code WrapperProperties}. It is shared by the request filter, request wrapper, argument resolver, and request
 * context utilities so that all components make consistent decisions for legacy and strict modes.
 * </p>
 * <p>
 * The default values intentionally preserve the historical behavior used by existing projects. Future behavior changes
 * should first be introduced as configurable switches here, and only then considered for default changes in a major
 * release.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WrapperRuntimeOptions {

    /**
     * Wrap all POST/PUT/PATCH requests. This preserves the legacy behavior.
     */
    public static final String WRAP_CONTENT_TYPES_ALL = "all";

    /**
     * Wrap only JSON and form requests.
     */
    public static final String WRAP_CONTENT_TYPES_JSON_FORM = "json-form";

    /**
     * Wrap only JSON requests.
     */
    public static final String WRAP_CONTENT_TYPES_JSON_ONLY = "json-only";

    /**
     * Shared runtime snapshot used by wrapper-related components when no explicit instance is injected.
     */
    private static volatile WrapperRuntimeOptions INSTANCE = WrapperRuntimeOptions.builder().build();

    /**
     * Whether request parameters and headers should be sanitized before being exposed to application code.
     */
    @Builder.Default
    private boolean sanitizeInputValues = true;

    /**
     * Whether an empty body may be synthesized from {@code parameterMap} for legacy form compatibility.
     */
    @Builder.Default
    private boolean synthesizeFormBody = true;

    /**
     * Whether the custom argument resolver should continue to resolve all non-simple controller parameters.
     */
    @Builder.Default
    private boolean resolveNonSimpleArguments = true;

    /**
     * Controls the request wrapping scope. Supported values are {@code all}, {@code json-form}, and {@code json-only}.
     */
    @Builder.Default
    private String wrapContentTypes = WRAP_CONTENT_TYPES_ALL;

    /**
     * Whether multipart requests should remain inside the wrapper compatibility scope.
     */
    @Builder.Default
    private boolean includeMultipart = true;

    /**
     * Returns the current shared runtime options snapshot.
     *
     * @return The current {@link WrapperRuntimeOptions} instance.
     */
    public static WrapperRuntimeOptions of() {
        return INSTANCE;
    }

    /**
     * Replaces the current shared runtime options snapshot.
     *
     * @param options The new runtime snapshot. If {@code null}, a default legacy-compatible instance is installed.
     */
    public static void update(WrapperRuntimeOptions options) {
        INSTANCE = options == null ? WrapperRuntimeOptions.builder().build() : options;
    }

    /**
     * Returns a copy of the current runtime snapshot with an updated wrapping scope.
     *
     * @param wrapContentTypes Supported values are {@code all}, {@code json-form}, and {@code json-only}. Blank input
     *                         falls back to {@code all}.
     * @return A new {@link WrapperRuntimeOptions} instance with the updated wrap mode.
     */
    public WrapperRuntimeOptions withWrapContentTypes(String wrapContentTypes) {
        return WrapperRuntimeOptions.builder().sanitizeInputValues(this.sanitizeInputValues)
                .synthesizeFormBody(this.synthesizeFormBody).resolveNonSimpleArguments(this.resolveNonSimpleArguments)
                .wrapContentTypes(wrapContentTypes).includeMultipart(this.includeMultipart).build();
    }

    /**
     * Sets the wrapping scope after applying the same normalization used by runtime lookups.
     *
     * @param wrapContentTypes Supported values are {@code all}, {@code json-form}, and {@code json-only}. Blank input
     *                         falls back to {@code all}.
     */
    public void setWrapContentTypes(String wrapContentTypes) {
        this.wrapContentTypes = normalizeWrapContentTypes(wrapContentTypes);
    }

    /**
     * Returns the normalized wrapping scope mode.
     *
     * @return The configured wrap content-type mode after blank values have been normalized to {@code all}.
     */
    public String getWrapContentTypes() {
        return normalizeWrapContentTypes(this.wrapContentTypes);
    }

    /**
     * Determines whether the given request should be wrapped according to the current runtime compatibility settings.
     *
     * @param request The current HTTP request.
     * @return {@code true} if the request should be wrapped, {@code false} otherwise.
     */
    public boolean shouldWrap(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String method = request.getMethod();
        if (!HTTP.POST.equals(method) && !HTTP.PUT.equals(method) && !HTTP.PATCH.equals(method)) {
            return false;
        }
        String contentType = request.getContentType();
        if (!includeMultipart && contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA)) {
            return false;
        }
        return switch (getWrapContentTypes()) {
            case WRAP_CONTENT_TYPES_JSON_FORM -> isJson(contentType) || isForm(contentType)
                    || (includeMultipart && isMultipart(contentType));
            case WRAP_CONTENT_TYPES_JSON_ONLY -> isJson(contentType);
            default -> true;
        };
    }

    /**
     * Determines whether the given content type represents a JSON request.
     *
     * @param contentType The raw content type string.
     * @return {@code true} if the content type is JSON-compatible.
     */
    private boolean isJson(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.startsWith(MediaType.APPLICATION_JSON) || contentType.contains("+json");
    }

    /**
     * Determines whether the given content type represents a URL-encoded form request.
     *
     * @param contentType The raw content type string.
     * @return {@code true} if the content type is form-urlencoded.
     */
    private boolean isForm(String contentType) {
        return contentType != null && contentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * Determines whether the given content type represents a multipart request.
     *
     * @param contentType The raw content type string.
     * @return {@code true} if the content type is multipart.
     */
    private boolean isMultipart(String contentType) {
        return contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA);
    }

    /**
     * Returns a normalized wrapping scope value.
     *
     * @param wrapContentTypes The raw wrap content-types setting.
     * @return A normalized non-blank mode value.
     */
    private static String normalizeWrapContentTypes(String wrapContentTypes) {
        return (wrapContentTypes == null || wrapContentTypes.isBlank()) ? WRAP_CONTENT_TYPES_ALL : wrapContentTypes;
    }

}
