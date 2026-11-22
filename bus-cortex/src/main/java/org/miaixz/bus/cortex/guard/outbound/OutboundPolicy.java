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
package org.miaixz.bus.cortex.guard.outbound;

import java.time.Duration;
import java.util.List;

import org.miaixz.bus.cortex.guard.GuardEvaluator.Rule;

/**
 * Outbound service identity configuration.
 *
 * @param enabled whether outgoing credentials may be issued
 * @param subject local service code
 * @param ttl     lifetime of the issued token
 * @param rules   configured destinations
 *
 * @author Kimi Liu
 */
public record OutboundPolicy(boolean enabled, String subject, Duration ttl, List<Rule> rules) {

    /**
     * Validates the active issuer settings and destination rules.
     *
     * @param enabled whether outgoing credentials may be issued
     * @param subject local service code
     * @param ttl     lifetime of an issued token
     * @param rules   configured destinations
     * @throws IllegalArgumentException if the rules are invalid or enabled signing has incomplete settings
     */
    public OutboundPolicy {
        rules = Rule.validateRules(rules, true);
        if (enabled && (subject == null || subject.isBlank() || ttl.toSeconds() < 60 || ttl.toSeconds() > 900
                || rules.isEmpty()))
            throw new IllegalArgumentException("Incomplete outbound service identity settings");
    }

}
