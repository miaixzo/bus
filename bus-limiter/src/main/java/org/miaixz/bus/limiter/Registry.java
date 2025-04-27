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
import org.miaixz.bus.limiter.metric.ResourceManager;
import org.miaixz.bus.logger.Logger;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;

/**
 * 管控规则
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Registry {

    /**
     * 请求降级
     *
     * @param downgrade   降级注解
     * @param resourceKey 资源标识
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
     * 提升热点
     *
     * @param hotspot     热点注解
     * @param resourceKey 资源标识
     */
    public static void register(Hotspot hotspot, String resourceKey) {
        // 仅在未配置时进行注册
        if (!ParamFlowRuleManager.hasRules(resourceKey)) {
            // 设置规则（base sentinel）
            ParamFlowRule rule = new ParamFlowRule();

            rule.setResource(resourceKey);
            rule.setGrade(hotspot.grade().getGrade());
            rule.setCount(hotspot.count());
            rule.setDurationInSec(hotspot.duration());
            rule.setParamIdx(0);

            ParamFlowRuleManager.loadRules(ListKit.of(rule));
            Logger.info("Add Hot Rule [{}]", rule.getResource());
        }
    }

    /**
     * 请求限流
     *
     * @param limiting    限流注解
     * @param resourceKey 资源标识
     */
    public static void register(Limiting limiting, String resourceKey) {
        if (!ResourceManager.contain(resourceKey)) {
            ResourceManager.add(resourceKey);
            Logger.info("Add Request Limit [{}]", resourceKey);
        }
    }

}
