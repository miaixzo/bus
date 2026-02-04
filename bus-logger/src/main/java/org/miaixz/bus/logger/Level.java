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
package org.miaixz.bus.logger;

/**
 * Defines the logging levels.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Level {

    /**
     * The 'ALL' level designates that all messages should be logged.
     */
    ALL,
    /**
     * The 'TRACE' level designates finer-grained informational events than the 'DEBUG' level.
     */
    TRACE,
    /**
     * The 'DEBUG' level designates fine-grained informational events that are most useful to debug an application.
     */
    DEBUG,
    /**
     * The 'INFO' level designates informational messages that highlight the progress of the application at a
     * coarse-grained level.
     */
    INFO,
    /**
     * The 'WARN' level designates potentially harmful situations.
     */
    WARN,
    /**
     * The 'ERROR' level designates error events that might still allow the application to continue running.
     */
    ERROR,
    /**
     * The 'FATAL' level designates very severe error events that will presumably lead the application to abort.
     */
    FATAL,
    /**
     * The 'OFF' level has the highest possible rank and is intended to turn off logging.
     */
    OFF

}
