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
 * Module: {@code bus.sensitive}
 *
 * <p>
 * Provides configurable masking and desensitization of sensitive data.
 *
 * <p>
 * Includes masking annotations, sanitization filters, strategy and provider registries, conditional processing, and
 * built-in rules for names, addresses, identity numbers, bank cards, email addresses, phone numbers, and passwords.
 *
 * @author Kimi Liu
 */
module bus.sensitive {

    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.logger;

    requires static lombok;

    exports org.miaixz.bus.sensitive;
    exports org.miaixz.bus.sensitive.magic.annotation;
    exports org.miaixz.bus.sensitive.nimble;

}
