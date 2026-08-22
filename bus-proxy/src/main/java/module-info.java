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
 * Module: {@code bus.proxy}
 *
 * <p>
 * Provides abstractions for creating and invoking dynamic proxies.
 *
 * <p>
 * Includes proxy factories, invocation and interceptor contracts, chained interception, JDK dynamic proxy support, and
 * Spring CGLIB integration.
 *
 * @author Kimi Liu
 */
module bus.proxy {

    requires bus.core;
    requires bus.logger;

    requires static org.aspectj.weaver;
    requires static spring.core;

    exports org.miaixz.bus.proxy;
    exports org.miaixz.bus.proxy.invoker;
    exports org.miaixz.bus.proxy.jdk;
    exports org.miaixz.bus.proxy.spring;

}
