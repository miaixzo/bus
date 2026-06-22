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
 * bus.storage
 *
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.storage {

    requires java.net.http;

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.gitlab;
    requires bus.http;
    requires bus.logger;

    requires static lombok;
    requires static com.github.sardine;
    requires static com.hierynomus.smbj;
    requires static com.jcraft.jsch;
    requires static org.reactivestreams;
    requires static software.amazon.awssdk.auth;
    requires static software.amazon.awssdk.core;
    requires static software.amazon.awssdk.http;
    requires static software.amazon.awssdk.http.auth;
    requires static software.amazon.awssdk.regions;
    requires static software.amazon.awssdk.services.s3;
    requires static software.amazon.awssdk.utils;

    exports org.miaixz.bus.storage;
    exports org.miaixz.bus.storage.cache;
    exports org.miaixz.bus.storage.magic;
    exports org.miaixz.bus.storage.metric;

}
