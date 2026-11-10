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
package org.miaixz.bus.errors.internal;

import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.errors.ErrorDescriptor;
import org.miaixz.bus.errors.ErrorEngine;
import org.miaixz.bus.errors.ErrorType;
import org.miaixz.bus.errors.policy.ErrorPolicyCompiler;

/**
 * Separates the safe structured error representation from diagnostic and legacy data.
 *
 * @author Kimi Liu
 */
public final class ErrorExposurePolicy {

    /**
     * Prevents construction of the stateless public projection utility.
     */
    private ErrorExposurePolicy() {
    }

    /**
     * Projects an error using only its registered message key and approved structured parameters.
     *
     * @param catalog public definitions
     * @param error   internal descriptor
     * @return public descriptor without a legacy code or diagnostic cause
     */
    public static ErrorDescriptor project(ErrorCatalog catalog, ErrorDescriptor error) {
        var definition = catalog.get(error.type());
        if (definition == null) {
            ErrorType type = ErrorPolicyCompiler.isStandardType(error.type()) ? error.type() : ErrorType.UNKNOWN;
            return ErrorEngine.descriptor(type);
        }
        var parameters = error.publicParameters().entrySet().stream()
                .filter(entry -> definition.publicParameters().contains(entry.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        return new ErrorDescriptor(error.type(), null, definition.messageKey(), parameters);
    }

}
