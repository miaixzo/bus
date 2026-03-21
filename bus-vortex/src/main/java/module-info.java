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
 * The core module for the Bus Vortex reactive API gateway.
 * <p>
 * This module provides a flexible, non-blocking, and extensible framework for building API gateways on top of the
 * Spring WebFlux and Project Reactor stack. It defines the core components for request processing, including the
 * strategy chain, routing, and various service provider interfaces.
 * <p>
 * Key dependencies:
 * <ul>
 * <li>{@code bus.core}, {@code bus.extra}, etc.: The foundational libraries of the Bus ecosystem, required
 * transitively.</li>
 * <li>{@code spring.webflux}, {@code reactor.core}: The reactive foundation for the entire gateway, marked as as
 * this is a framework.</li>
 * <li>{@code com.google.common}: Used for utilities like the {@code RateLimiter}.</li>
 * </ul>
 * <p>
 * Exported API Packages:
 * <ul>
 * <li>{@code org.miaixz.bus.vortex}: Core interfaces like {@code Strategy}, {@code Router}, and the central
 * {@code Context} object.</li>
 * <li>{@code org.miaixz.bus.vortex.filter}: The main entry point {@code PrimaryFilter}.</li>
 * <li>{@code org.miaixz.bus.vortex.strategy}: Concrete strategy implementations for the processing chain.</li>
 * <li>{@code org.miaixz.bus.vortex.handler}: Final request handlers and global error handling.</li>
 * <li>{@code org.miaixz.bus.vortex.provider}: Service Provider Interfaces (SPIs) for extensibility.</li>
 * <li>{@code org.miaixz.bus.vortex.registry}: In-memory registries for runtime configurations.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.vortex {

    requires bus.core;
    requires bus.cache;
    requires bus.crypto;
    requires bus.extra;
    requires bus.health;
    requires bus.http;
    requires bus.logger;

    requires lombok;
    requires jakarta.annotation;
    requires jakarta.servlet;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.web;
    requires spring.webflux;
    requires reactor.core;
    requires reactor.netty.http;
    requires reactor.netty.core;
    requires io.netty.handler;
    requires org.reactivestreams;
    requires com.google.common;

    exports org.miaixz.bus.vortex;
    exports org.miaixz.bus.vortex.filter;
    exports org.miaixz.bus.vortex.handler;
    exports org.miaixz.bus.vortex.magic;
    exports org.miaixz.bus.vortex.provider;
    exports org.miaixz.bus.vortex.registry;
    exports org.miaixz.bus.vortex.strategy;
    exports org.miaixz.bus.vortex.support;
    exports org.miaixz.bus.vortex.support.grpc;
    exports org.miaixz.bus.vortex.support.llm;
    exports org.miaixz.bus.vortex.support.mcp;
    exports org.miaixz.bus.vortex.support.mq;
    exports org.miaixz.bus.vortex.support.rest;
    exports org.miaixz.bus.vortex.support.ws;
    exports org.miaixz.bus.vortex.support.mcp.client;
    exports org.miaixz.bus.vortex.support.mcp.server;

}
