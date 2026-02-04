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
 * bus.logger
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.logger {

    requires java.logging;
    requires bus.core;

    requires org.jboss.logging;
    requires org.apache.commons.logging;
    requires org.apache.logging.log4j;
    requires org.slf4j;
    requires org.tinylog.api;
    requires ch.qos.logback.classic;

    exports org.miaixz.bus.logger;
    exports org.miaixz.bus.logger.magic;
    exports org.miaixz.bus.logger.magic.level;
    exports org.miaixz.bus.logger.metric.apache.commons;
    exports org.miaixz.bus.logger.metric.apache.log4j;
    exports org.miaixz.bus.logger.metric.console;
    exports org.miaixz.bus.logger.metric.jboss;
    exports org.miaixz.bus.logger.metric.jdk;
    exports org.miaixz.bus.logger.metric.slf4j;
    exports org.miaixz.bus.logger.metric.tinylog;

}
