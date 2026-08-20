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
package org.miaixz.bus.auth.protocol.saml;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Models the ordered SAML protocol {@code StatusType} sequence.
 *
 * @param statusCode    required top-level status code
 * @param statusMessage optional human-readable protocol status message
 * @param statusDetail  optional structured status detail extensions
 * @author Kimi Liu
 */
public record Status(StatusCode statusCode, Optional<StatusMessage> statusMessage,
        Optional<StatusDetail> statusDetail) {

    /**
     * Validates and normalizes the optional status children.
     *
     * @throws IllegalArgumentException if a component or optional container is {@code null}
     */
    public Status {
        Assert.notNull(statusCode, "SAML StatusCode must not be null");
        Assert.notNull(statusMessage, "SAML StatusMessage container must not be null");
        Assert.notNull(statusDetail, "SAML StatusDetail container must not be null");
        statusMessage = Optional.ofNullable(statusMessage.getOrNull());
        statusDetail = Optional.ofNullable(statusDetail.getOrNull());
    }

}
