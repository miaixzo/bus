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
 * Provides protocol-neutral authentication contracts, standard server protocol APIs, and third-party client contracts.
 *
 * <p>
 * Only stable root contracts, standard protocol surfaces, the vendor contract and catalog, and resolver contracts are
 * exported. Runtime assembly, codecs, state storage, guards, bridges, protocol implementations, routers, caches, and
 * concrete vendor clients remain module-internal. Sensitive credentials are resolved per operation and must not be
 * retained in exported values.
 * </p>
 *
 * @author Kimi Liu
 */
module bus.auth {

    requires java.naming;

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.fabric;
    requires bus.logger;

    requires static lombok;
    requires static org.bouncycastle.pkix;
    requires static org.bouncycastle.provider;

    exports org.miaixz.bus.auth;
    exports org.miaixz.bus.auth.protocol;
    exports org.miaixz.bus.auth.protocol.oauth2;
    exports org.miaixz.bus.auth.protocol.oidc;
    /** Exports LDAP standard protocol contracts. */
    exports org.miaixz.bus.auth.protocol.ldap;
    /** Exports RADIUS standard protocol contracts. */
    exports org.miaixz.bus.auth.protocol.radius;
    /** Exports SCIM standard protocol contracts. */
    exports org.miaixz.bus.auth.protocol.scim;
    /** Exports Shared Signals Framework standard protocol contracts. */
    exports org.miaixz.bus.auth.protocol.ssf;
    /** Exports third-party authentication client contracts. */
    exports org.miaixz.bus.auth.vendor;
    /** Exports the immutable built-in vendor definition catalog. */
    exports org.miaixz.bus.auth.vendor.catalog;
    /** Exports operation-scoped resolver contracts. */
    exports org.miaixz.bus.auth.resolver;
    exports org.miaixz.bus.auth.vendor.microsoft;
}
