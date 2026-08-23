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
 * Module: {@code bus.logger}
 *
 * <p>
 * Provides a logging facade independent of any specific logging framework.
 *
 * <p>
 * Includes common logger contracts, severity levels, message formatting, console logging, and adapters for Apache
 * Commons Logging, Log4j, JBoss Logging, JDK logging, SLF4J, and tinylog.
 *
 * @author Kimi Liu
 */
module bus.logger {

    requires java.logging;

    requires bus.core;

    requires static ch.qos.logback.classic;
    requires static org.apache.commons.logging;
    requires static org.apache.logging.log4j;
    requires static org.jboss.logging;
    requires static org.slf4j;
    requires static org.tinylog.api;

    exports org.miaixz.bus.logger;
    exports org.miaixz.bus.logger.magic;
    exports org.miaixz.bus.logger.magic.level;
    exports org.miaixz.bus.logger.nimble.apache.commons;
    exports org.miaixz.bus.logger.nimble.apache.log4j;
    exports org.miaixz.bus.logger.nimble.console;
    exports org.miaixz.bus.logger.nimble.jboss;
    exports org.miaixz.bus.logger.nimble.jdk;
    exports org.miaixz.bus.logger.nimble.slf4j;
    exports org.miaixz.bus.logger.nimble.tinylog;

}
