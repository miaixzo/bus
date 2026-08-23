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
 * Module: {@code bus.vortex}
 *
 * <p>
 * Provides a non-blocking and extensible API gateway built on Spring WebFlux and Project Reactor.
 *
 * <p>
 * Includes filters, handlers, strategy chains, provider interfaces, registries, cluster and cache integration, and
 * routing for REST, gRPC, WebSocket, message queues, large language models, and Model Context Protocol services.
 *
 * @author Kimi Liu
 */
module bus.vortex {

    requires bus.cache;
    requires bus.core;
    requires bus.cortex;
    requires bus.crypto;
    requires bus.extra;
    requires bus.logger;

    requires static io.netty.buffer;
    requires static io.netty.common;
    requires static io.netty.transport;
    requires static jakarta.annotation;
    requires static lombok;
    requires static org.reactivestreams;
    requires static reactor.core;
    requires static reactor.netty.core;
    requires static reactor.netty.http;
    requires static spring.beans;
    requires static spring.context;
    requires static spring.core;
    requires static spring.web;
    requires static spring.webflux;

    exports org.miaixz.bus.vortex;
    exports org.miaixz.bus.vortex.cache;
    exports org.miaixz.bus.vortex.cluster;
    exports org.miaixz.bus.vortex.filter;
    exports org.miaixz.bus.vortex.guard;
    exports org.miaixz.bus.vortex.handler;
    exports org.miaixz.bus.vortex.magic;
    exports org.miaixz.bus.vortex.nimble;
    exports org.miaixz.bus.vortex.provider;
    exports org.miaixz.bus.vortex.registry;
    exports org.miaixz.bus.vortex.routing;
    exports org.miaixz.bus.vortex.routing.grpc;
    exports org.miaixz.bus.vortex.routing.llm;
    exports org.miaixz.bus.vortex.routing.mcp;
    exports org.miaixz.bus.vortex.routing.mq;
    exports org.miaixz.bus.vortex.routing.rest;
    exports org.miaixz.bus.vortex.routing.slug;
    exports org.miaixz.bus.vortex.routing.ws;
    exports org.miaixz.bus.vortex.strategy;
    exports org.miaixz.bus.vortex.strategy.qualifier;
    exports org.miaixz.bus.vortex.strategy.request;
    exports org.miaixz.bus.vortex.strategy.vetting;

}
