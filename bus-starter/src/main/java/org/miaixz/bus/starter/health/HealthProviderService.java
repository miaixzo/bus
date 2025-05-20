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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Provider;
import org.miaixz.bus.health.builtin.TID;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 健康状态提供者服务类，用于管理和监控系统的健康状态及硬件信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HealthProviderService {

    /**
     * 健康状态配置属性
     */
    private HealthProperties properties;
    /**
     * 系统信息提供者，用于获取硬件和操作系统信息
     */
    private Provider provider;
    /**
     * Spring 应用事件发布器，用于发布可用性状态变更事件
     */
    private ApplicationEventPublisher publisher;
    /**
     * Spring 应用可用性接口，用于获取和检查当前的存活状态和就绪状态
     */
    private ApplicationAvailability availability;

    /**
     * 构造函数，初始化健康状态服务。
     *
     * @param properties   健康状态配置属性
     * @param provider     系统信息提供者
     * @param publisher    Spring 应用事件发布器
     * @param availability Spring 应用可用性接口
     */
    public HealthProviderService(HealthProperties properties, Provider provider, ApplicationEventPublisher publisher,
            ApplicationAvailability availability) {
        this.properties = properties;
        this.provider = provider;
        this.publisher = publisher;
        this.availability = availability;
    }

    /**
     * 获取系统健康状态信息。
     *
     * @param tid 监控类型（可选，默认为 liveness,readiness）
     * @return 操作结果，包含状态信息的 Message 对象
     */
    public Object healthz(String tid) {
        try {
            // 设置 tid：优先使用输入，次选配置 type，否则默认 liveness,readiness
            String defaultTids = TID.LIVENESS + Symbol.COMMA + TID.READINESS;
            tid = StringKit.isEmpty(tid) ? (properties == null || StringKit.isEmpty(properties.getType()) ? defaultTids
                    : properties.getType().toLowerCase()) : tid.toLowerCase();

            // 若 tid 无效，设为 liveness,readiness
            List<String> tidList = Arrays.asList(tid.split(Symbol.COMMA));
            if (tidList.stream().noneMatch(TID.ALL_TID::contains)) {
                tidList = Arrays.asList(TID.LIVENESS, TID.READINESS);
                Logger.debug("Invalid tid '{}', defaulting to liveness,readiness", tid);
            }

            // 获取监控信息
            Map<String, Object> result = new HashMap<>();
            result.put("requestId", ID.objectId());
            try {
                result.putAll(TID.ALL.equals(tid) ? provider.getAll() : provider.get(tidList));
            } catch (NumberFormatException e) {
                Logger.warn("Invalid number format in provider data for tid '{}': {}", tid, e.getMessage());
                // 返回默认值或空数据
                tidList.forEach(type -> append(type, result));
            }

            // 返回 Message 对象
            return result;
        } catch (Exception e) {
            Logger.error("Failed to retrieve health information for tid '{}': {}", tid, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode.EM_FAILURE)
                    .errmsg("Failed to retrieve health information: " + e.getMessage()).build();
        }
    }

    /**
     * 将存活状态改为 BROKEN，导致 Kubernetes 杀死并重启 pod。
     *
     * @return 操作结果及当前时间
     */
    public Object broken() {
        AvailabilityChangeEvent.publish(publisher, this, LivenessState.BROKEN);
        return builder(EnumValue.Probe.BROKEN);
    }

    /**
     * 将存活状态改为 CORRECT，表示 pod 正常运行。
     *
     * @return 操作结果及当前时间
     */
    public Object correct() {
        AvailabilityChangeEvent.publish(publisher, this, LivenessState.CORRECT);
        return builder(EnumValue.Probe.CORRECT);
    }

    /**
     * 将就绪状态改为 ACCEPTING_TRAFFIC，Kubernetes 将请求转发到此 pod。
     *
     * @return 操作结果及当前时间
     */
    public Object accept() {
        AvailabilityChangeEvent.publish(publisher, this, ReadinessState.ACCEPTING_TRAFFIC);
        return builder(EnumValue.Probe.ACCEPT);
    }

    /**
     * 将就绪状态改为 REFUSING_TRAFFIC，Kubernetes 拒绝外部请求。
     *
     * @return 操作结果及当前时间
     */
    public Object refuse() {
        AvailabilityChangeEvent.publish(publisher, this, ReadinessState.REFUSING_TRAFFIC);
        return builder(EnumValue.Probe.REFUSE);
    }

    /**
     * 创建健康状态探针操作的结果映射。
     *
     * @param probe 探针类型
     * @return 包含错误消息和时间戳的映射
     */
    public Object builder(EnumValue.Probe probe) {
        return Map.of("state", probe.getValue(), "timestamp:", DateKit.current());
    }

    /**
     * 根据类型添加系统或硬件信息到结果映射。
     *
     * @param type 类型标识
     * @param map  结果映射
     */
    public void append(String type, Map<String, Object> map) {
        switch (type.toLowerCase()) {
        case TID.LIVENESS:
            map.put(type, availability.getLivenessState());
            break;
        case TID.READINESS:
            map.put(type, availability.getReadinessState());
            break;
        default:
            try {
                provider.append(type, map);
            } catch (Exception e) {
                Logger.error("Failed to append health data for type {}: {}", type, e.getMessage(), e);
                map.put(type, "Error: " + e.getMessage());
            }
            break;
        }
    }

}