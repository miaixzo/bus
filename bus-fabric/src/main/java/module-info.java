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
 * Defines the bus fabric module.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.fabric {

    requires bus.core;
    requires bus.crypto;
    requires bus.logger;

    requires static jakarta.xml.soap;

    exports org.miaixz.bus.fabric;
    exports org.miaixz.bus.fabric.bridge;
    exports org.miaixz.bus.fabric.bridge.servlet;
    exports org.miaixz.bus.fabric.cache;
    exports org.miaixz.bus.fabric.codec;
    exports org.miaixz.bus.fabric.codec.body;
    exports org.miaixz.bus.fabric.codec.frame;
    exports org.miaixz.bus.fabric.guard;
    exports org.miaixz.bus.fabric.guard.body;
    exports org.miaixz.bus.fabric.guard.frame;
    exports org.miaixz.bus.fabric.guard.route;
    exports org.miaixz.bus.fabric.guard.tls;
    exports org.miaixz.bus.fabric.network;
    exports org.miaixz.bus.fabric.network.aio;
    exports org.miaixz.bus.fabric.network.dns;
    exports org.miaixz.bus.fabric.network.dns.suffix;
    exports org.miaixz.bus.fabric.network.kcp;
    exports org.miaixz.bus.fabric.network.proxy;
    exports org.miaixz.bus.fabric.network.tcp;
    exports org.miaixz.bus.fabric.network.tls;
    exports org.miaixz.bus.fabric.network.tls.cert;
    exports org.miaixz.bus.fabric.network.tls.context;
    exports org.miaixz.bus.fabric.network.udp;
    exports org.miaixz.bus.fabric.observe;
    exports org.miaixz.bus.fabric.observe.event;
    exports org.miaixz.bus.fabric.observe.metrics;
    exports org.miaixz.bus.fabric.observe.tags;
    exports org.miaixz.bus.fabric.observe.timing;
    exports org.miaixz.bus.fabric.observe.window;
    exports org.miaixz.bus.fabric.protocol;
    exports org.miaixz.bus.fabric.protocol.http;
    exports org.miaixz.bus.fabric.protocol.http.agent;
    exports org.miaixz.bus.fabric.protocol.http.auth;
    exports org.miaixz.bus.fabric.protocol.http.body;
    exports org.miaixz.bus.fabric.protocol.http.cache;
    exports org.miaixz.bus.fabric.protocol.http.retry;
    exports org.miaixz.bus.fabric.protocol.socket;
    exports org.miaixz.bus.fabric.protocol.socket.body;
    exports org.miaixz.bus.fabric.protocol.socket.frame;
    exports org.miaixz.bus.fabric.protocol.sse;
    exports org.miaixz.bus.fabric.protocol.sse.body;
    exports org.miaixz.bus.fabric.protocol.sse.event;
    exports org.miaixz.bus.fabric.protocol.sse.retry;
    exports org.miaixz.bus.fabric.protocol.stomp;
    exports org.miaixz.bus.fabric.protocol.stomp.body;
    exports org.miaixz.bus.fabric.protocol.stomp.broker;
    exports org.miaixz.bus.fabric.protocol.stomp.frame;
    exports org.miaixz.bus.fabric.protocol.websocket;
    exports org.miaixz.bus.fabric.protocol.websocket.body;
    exports org.miaixz.bus.fabric.protocol.websocket.frame;
    exports org.miaixz.bus.fabric.protocol.websocket.upgrade;
    exports org.miaixz.bus.fabric.registry;
    exports org.miaixz.bus.fabric.registry.connection;
    exports org.miaixz.bus.fabric.registry.policy;
    exports org.miaixz.bus.fabric.registry.route;
    exports org.miaixz.bus.fabric.runtime;
    exports org.miaixz.bus.fabric.runtime.dispatch;
    exports org.miaixz.bus.fabric.runtime.resource;

}
