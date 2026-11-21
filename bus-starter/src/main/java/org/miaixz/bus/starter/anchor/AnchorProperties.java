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
package org.miaixz.bus.starter.anchor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Binds the deployment-owned internal service identity settings under {@code bus.anchor}.
 *
 * @param inbound  inbound caller identity settings
 * @param outbound outbound service identity settings
 * @param secret   the only shared secret, Base64 encoded
 * @param rotate   period of the derived effective key, in seconds
 * @author Kimi Liu
 */
@ConfigurationProperties(GeniusBuilder.ANCHOR)
public record AnchorProperties(@DefaultValue Inbound inbound, @DefaultValue Outbound outbound,
        @DefaultValue(Normal.EMPTY) String secret, @DefaultValue("86400") long rotate) {

    /**
     * Inbound caller identity settings.
     *
     * @param enabled     whether the inbound filter is registered
     * @param header      header carrying the compact token
     * @param subjects    accepted subjects, comma separated
     * @param audience    required audience, the service code of this application
     * @param paths       protected paths relative to the context path
     * @param skewSeconds accepted clock displacement
     * @author Kimi Liu
     */
    public record Inbound(@DefaultValue(Normal.TRUE) boolean enabled,
            @DefaultValue(Http.Auth.SERVICE_TOKEN_HEADER) String header, @DefaultValue(Normal.EMPTY) String subjects,
            @DefaultValue(Normal.EMPTY) String audience,
            @DefaultValue(Symbol.SLASH + "internal" + Symbol.SLASH + Symbol.STAR + Symbol.STAR) String paths,
            @DefaultValue(Symbol.FIVE) int skewSeconds) {
    }

    /**
     * Outbound service identity settings.
     *
     * @param enabled          whether the outbound identity is installed
     * @param subject          issued subject
     * @param header           attached header name
     * @param audience         default audience used when the target does not provide one
     * @param ttlSeconds       token lifetime
     * @param apps             target application codes
     * @param registryAudience audience used for registry and snapshot calls, empty when disabled
     * @param paths            target path prefixes, empty for all paths of the selected applications
     * @author Kimi Liu
     */
    public record Outbound(@DefaultValue(Normal.TRUE) boolean enabled, @DefaultValue(Normal.EMPTY) String subject,
            @DefaultValue(Http.Auth.SERVICE_TOKEN_HEADER) String header, @DefaultValue(Normal.EMPTY) String audience,
            @DefaultValue("300") int ttlSeconds, @DefaultValue(Normal.EMPTY) String apps,
            @DefaultValue(Normal.EMPTY) String registryAudience, @DefaultValue(Normal.EMPTY) String paths) {
    }

}
