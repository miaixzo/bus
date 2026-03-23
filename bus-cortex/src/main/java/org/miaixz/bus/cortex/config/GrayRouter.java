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
package org.miaixz.bus.cortex.config;

import java.util.Map;

/**
 * Gray rule matcher.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GrayRouter {

    /**
     * Evaluates whether the request context satisfies the supplied gray rule.
     *
     * @param rule    gray routing rule to evaluate
     * @param context request context providing client IP and headers
     * @return {@code true} if the rule matches the request context
     */
    public boolean matches(GrayRule rule, RequestContext context) {
        if (rule == null) {
            return false;
        }
        if (context == null) {
            return false;
        }
        return switch (rule.getType()) {
            case IP_LIST -> rule.getIpList() != null && rule.getIpList().contains(context.getClientIp());
            case IP_RANGE -> matchesRange(rule.getIpRange(), context.getClientIp());
            case PERCENTAGE -> matchesPercentage(rule.getPercentage(), context.getClientIp());
            case HEADER -> matchesHeader(rule, context.headerView());
        };
    }

    /**
     * Matches a header-based gray rule against request headers.
     *
     * @param rule    gray rule containing header key and value requirements
     * @param headers request headers to inspect
     * @return {@code true} if the required header value is present
     */
    private boolean matchesHeader(GrayRule rule, Map<String, String> headers) {
        if (rule.getHeaderKey() == null || rule.getHeaderValue() == null) {
            return false;
        }
        return rule.getHeaderValue().equals(headers.get(rule.getHeaderKey()));
    }

    /**
     * Matches a percentage-based gray rule by hashing the client IP.
     *
     * @param percentage percentage of traffic to include
     * @param clientIp   client IP used as a stable hash key
     * @return {@code true} if the client falls into the selected percentage bucket
     */
    private boolean matchesPercentage(int percentage, String clientIp) {
        if (percentage <= 0) {
            return false;
        }
        if (percentage >= 100) {
            return true;
        }
        if (clientIp == null) {
            return false;
        }
        return Math.floorMod(clientIp.hashCode(), 100) < percentage;
    }

    /**
     * Matches a client IP against an inclusive IPv4 range.
     *
     * @param range    IPv4 range in {@code start-end} format
     * @param clientIp client IP to test
     * @return {@code true} if the client IP is inside the range
     */
    private boolean matchesRange(String range, String clientIp) {
        if (range == null || clientIp == null) {
            return false;
        }
        String[] segments = range.split("-", 2);
        if (segments.length != 2) {
            return false;
        }
        long target = toIpv4Long(clientIp);
        if (target < 0) {
            return false;
        }
        long start = toIpv4Long(segments[0].trim());
        long end = toIpv4Long(segments[1].trim());
        if (start < 0 || end < 0) {
            return false;
        }
        return target >= Math.min(start, end) && target <= Math.max(start, end);
    }

    /**
     * Converts an IPv4 address string into an unsigned long representation.
     *
     * @param ip IPv4 address string
     * @return numeric IPv4 value, or {@code -1} if the input is invalid
     */
    private long toIpv4Long(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return -1L;
        }
        long value = 0L;
        for (String part : parts) {
            int segment;
            try {
                segment = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                return -1L;
            }
            if (segment < 0 || segment > 255) {
                return -1L;
            }
            value = (value << 8) | segment;
        }
        return value;
    }

}
