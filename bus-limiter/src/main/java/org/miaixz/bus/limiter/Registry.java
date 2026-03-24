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
package org.miaixz.bus.limiter;

import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.limiter.magic.annotation.Downgrade;
import org.miaixz.bus.limiter.magic.annotation.Hotspot;
import org.miaixz.bus.limiter.magic.annotation.Limiting;
import org.miaixz.bus.logger.Logger;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

/**
 * Manages the registration of various limiting and protection rules, such as downgrade, hotspot, and request limiting.
 * This class interacts with Sentinel's {@link FlowRuleManager} to configure flow control rules dynamically.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Registry {

    /**
     * Registers a downgrade rule based on the provided {@link Downgrade} annotation. If a flow rule for the given
     * resource key does not already exist, a new {@link FlowRule} is created with the specified grade and count, and
     * then loaded into Sentinel's {@link FlowRuleManager}.
     *
     * @param downgrade   The {@link Downgrade} annotation containing the downgrade configuration.
     * @param resourceKey The unique identifier for the resource to which the downgrade rule applies.
     */
    public static void register(Downgrade downgrade, String resourceKey) {
        if (!FlowRuleManager.hasConfig(resourceKey)) {
            FlowRule rule = new FlowRule();
            rule.setResource(resourceKey);
            rule.setGrade(downgrade.grade().getGrade());
            rule.setCount(downgrade.count());
            rule.setLimitApp("default");

            FlowRuleManager.loadRules(ListKit.of(rule));
            Logger.info("Add Fallback Rule [{}]", resourceKey);
        }
    }

    /**
     * Registers a hotspot rule based on the provided {@link Hotspot} annotation. If a flow rule for the given resource
     * key does not already exist, a new {@link FlowRule} is created with the specified grade and count. Note that
     * Sentinel versions like 1.8.8 might not fully support hotspot parameter flow control directly, and it might be
     * simplified to a regular flow control rule.
     *
     * @param hotspot     The {@link Hotspot} annotation containing the hotspot configuration.
     * @param resourceKey The unique identifier for the resource to which the hotspot rule applies.
     */
    public static void register(Hotspot hotspot, String resourceKey) {
        if (!FlowRuleManager.hasConfig(resourceKey)) {
            FlowRule rule = new FlowRule();
            rule.setResource(resourceKey);
            rule.setGrade(hotspot.grade().getGrade());
            rule.setCount(hotspot.count());
            rule.setLimitApp("default");
            // Note: sentinel-core 1.8.8 does not support hotspot parameter flow control, simplified to a normal flow
            // control rule
            // If hotspot functionality is needed, Sentinel needs to be extended or another framework used

            FlowRuleManager.loadRules(ListKit.of(rule));
            Logger.info("Add Hot Rule [{}]", rule.getResource());
        }
    }

    /**
     * Registers a request limiting rule based on the provided {@link Limiting} annotation. If a flow rule for the given
     * resource key does not already exist, a new {@link FlowRule} is created with a default QPS grade and the specified
     * count, then loaded into Sentinel's {@link FlowRuleManager}.
     *
     * @param limiting    The {@link Limiting} annotation containing the limiting configuration.
     * @param resourceKey The unique identifier for the resource to which the limiting rule applies.
     */
    public static void register(Limiting limiting, String resourceKey) {
        if (!FlowRuleManager.hasConfig(resourceKey)) {
            FlowRule rule = new FlowRule();
            rule.setResource(resourceKey);
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS); // Default QPS limiting
            rule.setCount(limiting.count());
            rule.setLimitApp("default");

            FlowRuleManager.loadRules(ListKit.of(rule));
            Logger.info("Add Request Limit [{}]", resourceKey);
        }
    }

}
