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
 * Module: {@code bus.errors}
 *
 * <p>
 * Provides shared failure classification and processing for Bus components.
 *
 * <p>
 * Includes component boundaries, error log policy, isolated diagnostics, route policies, recovery decisions, and
 * optional HTTP, Servlet, MVC, and reactive adapters.
 *
 * @author Kimi Liu
 */
module bus.errors {

    requires bus.core;
    requires bus.logger;

    requires static jakarta.servlet;
    requires static reactor.core;
    requires static spring.context;
    requires static spring.core;
    requires static spring.web;
    requires static spring.webflux;
    requires static spring.webmvc;

    exports org.miaixz.bus.errors;
    exports org.miaixz.bus.errors.policy;
    exports org.miaixz.bus.errors.recovery;
    exports org.miaixz.bus.errors.spi;
    exports org.miaixz.bus.errors.transport;
    exports org.miaixz.bus.errors.transport.http;
    exports org.miaixz.bus.errors.transport.mvc;
    exports org.miaixz.bus.errors.transport.reactive;
    exports org.miaixz.bus.errors.transport.servlet;

}
