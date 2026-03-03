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
package org.miaixz.bus.starter.vortex;

import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.magic.Performance;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for the Vortex routing gateway.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(GeniusBuilder.VORTEX)
public class VortexProperties {

    /**
     * The service port, specifying the port number the server listens on.
     */
    private int port;

    /**
     * The service path, specifying the access path for the server.
     */
    private String path;

    /**
     * A condition to enable or disable custom Spring MVC configuration handling.
     */
    private boolean condition;

    /**
     * Rate limiting configuration, initialized by default.
     */
    private Args.Limit limit = new Args.Limit();

    /**
     * Performance optimization settings for request body processing and connection pooling.
     * <p>
     * These settings allow fine-tuning of memory usage and throughput trade-offs.
     */
    private Performance performance = new Performance();

}
