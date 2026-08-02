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
package org.miaixz.bus.starter.mapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.miaixz.bus.mapper.feature.tenant.MissingTenantException;

/**
 * Maps a missing authenticated tenant to a value-free HTTP 403 response.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@RestControllerAdvice
public final class TenantExceptionAdvice {

    /**
     * Initializes stateless exception advice for requests that lack an authenticated tenant.
     */
    public TenantExceptionAdvice() {
        // No initialization required.
    }

    /**
     * Returns a fixed forbidden response without exposing authentication state.
     *
     * @param exception missing authenticated tenant failure
     * @return empty forbidden response
     */
    @ExceptionHandler(MissingTenantException.class)
    public ResponseEntity<Void> handleMissingTenant(MissingTenantException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

}
