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
 * Module: {@code bus.validate}
 *
 * <p>
 * Provides composable validation rules for application and domain data.
 *
 * <p>
 * Includes validation annotations, matchers, criteria, rule groups, contextual validation, error collection, range and
 * comparison checks, and built-in rules for common text, number, date, network, and identity formats.
 *
 * @author Kimi Liu
 */
module bus.validate {

    requires bus.core;
    requires bus.logger;

    requires static lombok;

    exports org.miaixz.bus.validate;
    exports org.miaixz.bus.validate.magic;
    exports org.miaixz.bus.validate.magic.annotation;
    exports org.miaixz.bus.validate.nimble;

}
