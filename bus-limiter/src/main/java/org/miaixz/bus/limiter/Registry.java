/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
