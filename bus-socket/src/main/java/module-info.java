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
 * bus.socket
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.socket {

    requires bus.core;
    requires bus.logger;

    exports org.miaixz.bus.socket;
    exports org.miaixz.bus.socket.accord;
    exports org.miaixz.bus.socket.accord.kcp;
    exports org.miaixz.bus.socket.buffer;
    exports org.miaixz.bus.socket.metric;
    exports org.miaixz.bus.socket.metric.channel;
    exports org.miaixz.bus.socket.metric.decoder;
    exports org.miaixz.bus.socket.metric.handler;
    exports org.miaixz.bus.socket.metric.message;
    exports org.miaixz.bus.socket.plugin;
    exports org.miaixz.bus.socket.secure;
    exports org.miaixz.bus.socket.secure.factory;

}
