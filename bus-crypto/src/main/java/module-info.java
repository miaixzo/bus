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
 * bus.crypto
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.crypto {

    requires java.security.sasl;
    requires java.xml;

    requires bus.core;
    requires bus.logger;

    requires lombok;
    requires org.bouncycastle.provider;
    requires org.bouncycastle.pkix;

    exports org.miaixz.bus.crypto;
    exports org.miaixz.bus.crypto.builtin;
    exports org.miaixz.bus.crypto.builtin.asymmetric;
    exports org.miaixz.bus.crypto.builtin.digest;
    exports org.miaixz.bus.crypto.builtin.symmetric;
    exports org.miaixz.bus.crypto.center;
    exports org.miaixz.bus.crypto.cipher;
    exports org.miaixz.bus.crypto.metric;

}
