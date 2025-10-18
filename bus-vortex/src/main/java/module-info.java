/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * <li>{@code spring.webflux}, {@code reactor.core}: The reactive foundation for the entire gateway, marked as static as
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

    requires transitive bus.core;
    requires transitive bus.crypto;
    requires transitive bus.extra;
    requires transitive bus.health;
    requires transitive bus.http;
    requires transitive bus.logger;

    requires static lombok;
    requires static jakarta.annotation;
    requires static jakarta.servlet;
    requires static spring.core;
    requires static spring.beans;
    requires static spring.context;
    requires static spring.web;
    requires static spring.webflux;
    requires static reactor.core;
    requires static reactor.netty.http;
    requires static reactor.netty.core;
    requires static io.netty.handler;
    requires static org.reactivestreams;
    requires static com.google.common;

    exports org.miaixz.bus.vortex;
    exports org.miaixz.bus.vortex.filter;
    exports org.miaixz.bus.vortex.handler;
    exports org.miaixz.bus.vortex.magic;
    exports org.miaixz.bus.vortex.provider;
    exports org.miaixz.bus.vortex.registry;
    exports org.miaixz.bus.vortex.strategy;
    exports org.miaixz.bus.vortex.support;
    exports org.miaixz.bus.vortex.support.mcp;
    exports org.miaixz.bus.vortex.support.mq;
    exports org.miaixz.bus.vortex.support.rest;
    exports org.miaixz.bus.vortex.support.mcp.client;
    exports org.miaixz.bus.vortex.support.mcp.server;

}
