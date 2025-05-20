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
package org.miaixz.bus.starter.health;

import org.miaixz.bus.logger.Logger;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听系统可用性事件的类。 基于 Spring 的事件监听机制，捕获系统可用性状态变更事件，并根据不同状态（存活状态和就绪状态）执行特定的行动。 集成 HealthProviderService
 * 以获取系统和硬件信息，用于日志记录、恢复操作或通知触发。
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
public class AvailabilityListener {

    /**
     * 监听 AvailabilityChangeEvent 事件，记录状态变更并根据状态类型执行特定动作。 支持以下状态类型：
     * <ul>
     * <li>{@link LivenessState#CORRECT}: 系统存活正常，记录调试日志。</li>
     * <li>{@link LivenessState#BROKEN}: 系统存活异常，记录调试日志，可能触发 Kubernetes 重启 pod。</li>
     * <li>{@link ReadinessState#ACCEPTING_TRAFFIC}: 系统就绪接受流量，记录调试日志。</li>
     * <li>{@link ReadinessState#REFUSING_TRAFFIC}: 系统拒绝流量，记录调试日志，可能移除 pod 的服务端点。</li>
     * </ul>
     *
     * @param event 可用性状态变更事件，包含状态和时间戳
     */
    @EventListener
    public void onStateChange(AvailabilityChangeEvent<? extends AvailabilityState> event) {
        AvailabilityState state = event.getState();
        long timestamp = event.getTimestamp();
        String stateName = state.toString();

        // 记录状态变更日志，包含状态类型和时间戳
        switch (state) {
        case ReadinessState.ACCEPTING_TRAFFIC -> Logger.debug("System is ready to accept traffic at {}: {}", timestamp,
                stateName);
        case ReadinessState.REFUSING_TRAFFIC -> Logger.debug("System is refusing traffic at {}: {}", timestamp,
                stateName);
        case LivenessState.BROKEN -> Logger.debug("System is in broken state at {}: {}", timestamp, stateName);
        case LivenessState.CORRECT -> Logger.debug("System is in correct state at {}: {}", timestamp, stateName);
        default -> Logger.warn("Unknown availability state detected at {}: {}", timestamp, stateName);
        }
    }

}