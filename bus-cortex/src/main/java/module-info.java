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

/**
 * Bus Cortex — unified registry and configuration center platform.
 * <p>
 * Plugin-based architecture with core abstraction layer supporting API/MCP/Prompt/Config registration types. Zero
 * third-party dependencies — uses only bus ecosystem modules (bus-cache, bus-extra, bus-logger). Storage via bus-cache
 * CacheX abstraction (Memory/Redis/JDBC).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.cortex {

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.health;
    requires bus.http;
    requires bus.logger;

    requires lombok;
    requires jakarta.persistence;

    exports org.miaixz.bus.cortex;
    exports org.miaixz.bus.cortex.bridge;
    exports org.miaixz.bus.cortex.builtin;
    exports org.miaixz.bus.cortex.config;
    exports org.miaixz.bus.cortex.guard;
    exports org.miaixz.bus.cortex.health;
    exports org.miaixz.bus.cortex.magic;
    exports org.miaixz.bus.cortex.registry;
    exports org.miaixz.bus.cortex.version;
    exports org.miaixz.bus.cortex.builtin.batch;
    exports org.miaixz.bus.cortex.builtin.event;
    exports org.miaixz.bus.cortex.builtin.graph;
    exports org.miaixz.bus.cortex.guard.token;
    exports org.miaixz.bus.cortex.magic.identity;
    exports org.miaixz.bus.cortex.magic.state;
    exports org.miaixz.bus.cortex.registry.api;
    exports org.miaixz.bus.cortex.registry.mcp;
    exports org.miaixz.bus.cortex.registry.prompt;

}
