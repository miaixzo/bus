/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
/**
 * bus.http
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.http {

    requires bus.core;
    requires bus.logger;

    requires lombok;
    requires jakarta.xml.soap;

    exports org.miaixz.bus.http;
    exports org.miaixz.bus.http.accord;
    exports org.miaixz.bus.http.accord.platform;
    exports org.miaixz.bus.http.bodys;
    exports org.miaixz.bus.http.cache;
    exports org.miaixz.bus.http.metric;
    exports org.miaixz.bus.http.metric.anget;
    exports org.miaixz.bus.http.metric.http;
    exports org.miaixz.bus.http.metric.proxy;
    exports org.miaixz.bus.http.metric.suffix;
    exports org.miaixz.bus.http.plugin.httpv;
    exports org.miaixz.bus.http.plugin.httpx;
    exports org.miaixz.bus.http.plugin.httpz;
    exports org.miaixz.bus.http.plugin.soap;
    exports org.miaixz.bus.http.secure;
    exports org.miaixz.bus.http.socket;

}
