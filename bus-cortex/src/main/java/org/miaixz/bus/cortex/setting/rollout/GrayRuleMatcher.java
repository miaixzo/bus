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
package org.miaixz.bus.cortex.setting.rollout;

import java.math.BigInteger;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.net.ip.IPv4;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.crypto.Builder;

/**
 * Stateless selector consuming current process data.
 *
 * @author Kimi Liu
 */
public final class GrayRuleMatcher {

    /**
     * Creates a stateless matcher.
     */
    public GrayRuleMatcher() {
        // No initialization required.
    }

    /**
     * Matches only a verified instance.
     *
     * @param rule     frozen selector
     * @param instance current process data
     * @return match
     */
    public boolean matches(GrayRule rule, Instance instance) {
        if (rule == null || instance == null || instance.getTenant_id() == null || instance.getChecksum() == null)
            return false;
        rule.validate();
        return switch (rule.getType()) {
            case IP_LIST -> rule.getIpList().contains(instance.getHost());
            case IP_RANGE -> inRange(rule.getIpRange(), instance.getHost());
            case PERCENTAGE -> new BigInteger(Builder.sha256(instance.getChecksum()), 16).mod(BigInteger.valueOf(100))
                    .intValue() < rule.getPercentage();
            case TENANT -> rule.getTenant().equals(instance.getTenant_id());
            case LABEL -> {
                Map<String, String> labels = instance.labelView();
                yield rule.getLabelValue().equals(labels.get(rule.getLabelKey()));
            }
        };
    }

    /**
     * Validates a supported inclusive IPv4 range.
     *
     * @param range source range
     * @return validity
     */
    public boolean validRange(String range) {
        if (range == null)
            return false;
        String[] parts = range.split("-", -1);
        if (parts.length != 2)
            return false;
        long a = address(parts[0].trim()), b = address(parts[1].trim());
        return a >= 0 && b >= a;
    }

    /**
     * Tests an instance address in the validated range.
     *
     * @param range selector
     * @param host  trusted address
     * @return match
     */
    private boolean inRange(String range, String host) {
        String[] parts = range.split("-", -1);
        long value = address(host);
        return value >= 0 && value >= address(parts[0].trim()) && value <= address(parts[1].trim());
    }

    /**
     * Uses the existing IPv4 tool after strict input validation.
     *
     * @param host address
     * @return numeric address or -1
     */
    private long address(String host) {
        return host != null && PatternKit.isMatch(Pattern.IPV4_PATTERN, host) ? IPv4.ipv4ToLong(host) : -1;
    }

}
