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
 * Provides a cron-like task scheduling module.
 * <p>
 * This module is inspired by Cron4j and supports second-level granularity as well as an optional year field, ensuring
 * compatibility with Crontab, Cron4j, and Quartz expressions. The main components of the cron module are:
 * <ul>
 * <li>{@link org.miaixz.bus.cron.Scheduler} The central scheduler for managing task lifecycles (add, remove, start,
 * stop).</li>
 * <li>{@link org.miaixz.bus.cron.crontab.Crontab} An interface for defining the tasks to be executed.</li>
 * <li>{@link org.miaixz.bus.cron.pattern.CronPattern} Represents a cron expression for defining task trigger
 * times.</li>
 * </ul>
 * <p>
 * For convenience, the {@link org.miaixz.bus.cron.Builder} utility class is provided to manage a global
 * {@link org.miaixz.bus.cron.Scheduler} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cron;
