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
 * The only exported starter package. It provides EnableAuth, EnableCache, EnableCors, EnableCortex, EnableDubbo,
 * EnableElastic, EnableFabric, EnableHealth, EnableI18n, EnableImage, EnableJdbc, EnableJson, EnableLimiter,
 * EnableMapper, EnableMetrics, EnableMongo, EnableNotify, EnableOffice, EnablePay, EnableSensitive, EnableStorage,
 * EnableTempus, EnableTracer, EnableValidate, EnableVortex, EnableWrapper, and EnableZookeeper.
 * <p>
 * Every {@code EnableXxx} annotation has higher activation priority than its corresponding {@code bus.xxx.enabled}
 * property. An explicit annotation therefore enables its feature even when the property is absent or set to
 * {@code false}; the property controls activation only when the annotation is absent.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.starter.annotation;
